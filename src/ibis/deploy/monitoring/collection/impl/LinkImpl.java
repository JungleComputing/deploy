package ibis.deploy.monitoring.collection.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.*;
import ibis.deploy.monitoring.collection.Metric.*;
import ibis.deploy.monitoring.collection.MetricDescription.*;
import ibis.deploy.monitoring.collection.exceptions.*;

/**
 * An interface for a link between two elements that exist within the managed
 * universe.
 * 
 * @author Maarten van Meersbergen
 */
public class LinkImpl extends ElementImpl implements Link {
    private static final Logger logger = LoggerFactory
            .getLogger("ibis.deploy.monitoring.collection.impl.Link");

    private CollectorImpl c;

    private ArrayList<Link> children;
    private ElementImpl source;
    private ElementImpl destination;
    private HashMap<MetricDescription, Metric> srcToDstMetrics,
            dstToSrcMetrics;
    private boolean updatedThisCycle;

    public LinkImpl(ElementImpl origin, Element destination) {
        super();
        this.source = origin;
        this.destination = (ElementImpl) destination;

        try {
            c = CollectorImpl.getCollector();
        } catch (SingletonObjectNotInstantiatedException e) {
            e.printStackTrace();
        }

        updatedThisCycle = false;

        srcToDstMetrics = new HashMap<MetricDescription, Metric>();
        dstToSrcMetrics = new HashMap<MetricDescription, Metric>();

        children = new ArrayList<Link>();
    }

    @Override
    public Metric[] getMetrics() {
        System.out
                .println("Not implemented, and not meant to be implemented. Use getMetrics(LinkDirection dir); instead");
        return null;
    }

    @Override
    public Metric getMetric(MetricDescription desc)
            throws MetricNotAvailableException {
        System.out
                .println("Not implemented, and not meant to be implemented. Use getMetrics(LinkDirection dir); instead");
        return null;
    }

    @Override
    public Metric[] getMetrics(LinkDirection dir) {
        ArrayList<Metric> result = new ArrayList<Metric>();

        if (dir == LinkDirection.SRC_TO_DST) {
            for (Metric metric : srcToDstMetrics.values()) {
                result.add(metric);
            }
        } else {
            for (Metric metric : dstToSrcMetrics.values()) {
                result.add(metric);
            }
        }

        return result.toArray(new Metric[0]);
    }

    @Override
    public Metric getMetric(MetricDescription desc, LinkDirection dir)
            throws MetricNotAvailableException {
        if (dir == LinkDirection.SRC_TO_DST
                && srcToDstMetrics.containsKey(desc)) {
            return srcToDstMetrics.get(desc);
        } else if (dir == LinkDirection.DST_TO_SRC
                && dstToSrcMetrics.containsKey(desc)) {
            return dstToSrcMetrics.get(desc);
        } else {
            throw new MetricNotAvailableException();
        }
    }

    @Override
    public ElementImpl getSource() {
        return source;
    }

    @Override
    public ElementImpl getDestination() {
        return destination;
    }

    public int getNumberOfDescendants() {
        if (source instanceof IbisImpl && destination instanceof IbisImpl) {
            return 1;
        } else {
            int result = 0;
            for (Link child : children) {
                result += ((LinkImpl) child).getNumberOfDescendants();
            }

            return result;
        }
    }

    public void addChild(Link newChild) {
        if (!children.contains(newChild)) {
            children.add(newChild);
        }
    }

    public void update() {
        if (!updatedThisCycle) {
            // First update all of our children
            for (Link child : children) {
                ((LinkImpl) child).update();
            }

            if (source instanceof IbisImpl && destination instanceof IbisImpl) {
                updateIbisIbisLink(srcToDstMetrics, source, destination);
                updateIbisIbisLink(dstToSrcMetrics, destination, source);
            } else if (source instanceof LocationImpl
                    || destination instanceof LocationImpl) {
                updateElementElementLink(srcToDstMetrics, source, destination);
                updateElementElementLink(dstToSrcMetrics, destination, source);
            } else {
                logger.error("Tried to update a Link between weird elements.");
            }
        }

        updatedThisCycle = true;
    }

    private void updateIbisIbisLink(HashMap<MetricDescription, Metric> map,
            Element source, Element destination) {
        // Transform the Link value maps into 'normal' metrics that apply to
        // this link
        for (Entry<MetricDescription, Metric> data : map.entrySet()) {
            MetricDescription desc = data.getKey();
            MetricImpl metric = (MetricImpl) data.getValue();

            ArrayList<MetricOutput> types = desc.getOutputTypes();
            for (MetricOutput outputtype : types) {
                try {
                    // First, we gather our own metrics
                    MetricImpl srcMetric = (MetricImpl) source.getMetric(desc);

                    Number srcLinkValue = srcMetric.getValue(
                            MetricModifier.NORM, outputtype, destination);

                    if (outputtype == MetricOutput.PERCENT
                            || outputtype == MetricOutput.R
                            || outputtype == MetricOutput.RPOS) {
                        float srcValue = srcLinkValue.floatValue();
                        metric.setValue(MetricModifier.NORM, outputtype,
                                srcValue);
                    } else { // We are MetricOutput.N
                        long srcValue = srcLinkValue.longValue();
                        metric.setValue(MetricModifier.NORM, outputtype,
                                srcValue);
                    }
                } catch (OutputUnavailableException impossible) {
                    // Impossible since we tested if it was available first.
                    impossible.printStackTrace();
                    logger.error("The impossible OutputUnavailableException just happened anyway.");
                } catch (BeyondAllowedRangeException impossible) {
                    // Impossible unless one of the children has a value that is
                    // already bad
                    impossible.printStackTrace();
                    logger.debug("The impossible BeyondAllowedRangeException just happened anyway.");
                } catch (MetricNotAvailableException impossible) {
                    impossible.printStackTrace();
                    logger.error("The impossible MetricNotAvailableException just happened anyway.");
                }
            }
        }
    }

    private void updateElementElementLink(
            HashMap<MetricDescription, Metric> map, Element source,
            Element destination) {
        // Aggregate 'Normal' Metrics from the children of this link, made
        // during the linkHierarchy process
        for (Entry<MetricDescription, Metric> data : map.entrySet()) {
            MetricDescription desc = data.getKey();
            MetricImpl metric = (MetricImpl) data.getValue();

            try {
                ArrayList<MetricOutput> types = desc.getOutputTypes();

                for (MetricOutput outputtype : types) {
                    if (outputtype == MetricOutput.PERCENT
                            || outputtype == MetricOutput.R
                            || outputtype == MetricOutput.RPOS) {
                        float total = 0f, max = -10000000f, min = 10000000f;

                        if (outputtype == MetricOutput.PERCENT) {
                            // We add up the metric values of our child
                            // locations, and divide

                            for (Link child : children) {
                                LinkDirection childDirection;
                                if (c.isAncestorOf(child, source)) {
                                    childDirection = LinkDirection.SRC_TO_DST;
                                } else {
                                    childDirection = LinkDirection.DST_TO_SRC;
                                }

                                float childValue = child
                                        .getMetric(desc, childDirection)
                                        .getValue(MetricModifier.NORM,
                                                MetricOutput.PERCENT)
                                        .floatValue();
                                total += childValue;

                                if (childValue > max)
                                    max = childValue;
                                if (childValue < min)
                                    min = childValue;
                            }

                            float percent = total / children.size();
                            if (percent < 0f)
                                percent = 0f;
                            if (percent > 1f)
                                percent = 1f;

                            if (max < 0)
                                max = 0;
                            if (min < 0)
                                min = 0;

                            if (max > 1f)
                                max = 1f;
                            if (min > 1f)
                                min = 1f;

                            metric.setValue(MetricModifier.NORM, outputtype,
                                    percent);
                            metric.setValue(MetricModifier.MAX, outputtype, max);
                            metric.setValue(MetricModifier.MIN, outputtype, min);
                        } else {
                            // We add up the metric values of our child
                            // locations
                            for (Link child : children) {
                                LinkDirection childDirection;
                                if (c.isAncestorOf(child, source)) {
                                    childDirection = LinkDirection.SRC_TO_DST;
                                } else {
                                    childDirection = LinkDirection.DST_TO_SRC;
                                }
                                float childValue = child
                                        .getMetric(desc, childDirection)
                                        .getValue(MetricModifier.NORM,
                                                outputtype).floatValue();

                                total += childValue;

                                if (childValue > max)
                                    max = childValue;
                                if (childValue < min)
                                    min = childValue;

                            }

                            if (max < 0)
                                max = 0;
                            if (min < 0)
                                min = 0;

                            if (max > 1f)
                                max = 1f;
                            if (min > 1f)
                                min = 1f;

                            metric.setValue(MetricModifier.NORM, outputtype,
                                    total);
                            metric.setValue(MetricModifier.MAX, outputtype, max);
                            metric.setValue(MetricModifier.MIN, outputtype, min);
                        }
                    } else { // We are MetricOutput.N
                        long total = 0, max = 0, min = 1000000;

                        // We add up the metric values of our child locations
                        for (Link child : children) {
                            long childValue = child
                                    .getMetric(desc, LinkDirection.SRC_TO_DST)
                                    .getValue(MetricModifier.NORM, outputtype)
                                    .longValue();

                            total += childValue;

                            if (childValue > max)
                                max = childValue;
                            if (childValue < min)
                                min = childValue;
                        }

                        if (max < 0)
                            max = 0;
                        if (min < 0)
                            min = 0;

                        metric.setValue(MetricModifier.NORM, outputtype, total);
                        metric.setValue(MetricModifier.MAX, outputtype, max);
                        metric.setValue(MetricModifier.MIN, outputtype, min);
                    }
                }
            } catch (OutputUnavailableException impossible) {
                // Impossible since we tested if it was available first.
                impossible.printStackTrace();
                logger.error("The impossible OutputUnavailableException just happened anyway.");
            } catch (BeyondAllowedRangeException impossible) {
                // Impossible unless one of the children has a value that is
                // already bad
                impossible.printStackTrace();
                logger.debug("The impossible BeyondAllowedRangeException just happened anyway.");
            } catch (MetricNotAvailableException impossible) {
                impossible.printStackTrace();
                logger.error("The impossible MetricNotAvailableException just happened anyway.");
            }
        }
    }

    @Override
    public void setMetrics(Set<MetricDescription> descriptions) {

        // add new metrics
        for (MetricDescription md : descriptions) {

            if (!srcToDstMetrics.containsKey(md)
                    && md.getType() == MetricType.LINK) {
                MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) md)
                        .getMetric(this);
                srcToDstMetrics.put(md, newMetric);
            }

            if (!dstToSrcMetrics.containsKey(md)
                    && md.getType() == MetricType.LINK) {
                MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) md)
                        .getMetric(this);
                dstToSrcMetrics.put(md, newMetric);
            }
        }

        // make a snapshot of our current metrics.
        Set<MetricDescription> temp = new HashSet<MetricDescription>();
        temp.addAll(srcToDstMetrics.keySet());

        // and loop through the snapshot to remove unwanted metrics
        for (MetricDescription entry : temp) {
            if (!descriptions.contains(entry)) {
                srcToDstMetrics.remove(entry);
            }
        }

        // make a snapshot of our current metrics.
        temp = new HashSet<MetricDescription>();
        temp.addAll(dstToSrcMetrics.keySet());

        // and loop through the snapshot to remove unwanted metrics
        for (MetricDescription entry : temp) {
            if (!descriptions.contains(entry)) {
                dstToSrcMetrics.remove(entry);
            }
        }
    }

    @Override
    public void addMetric(MetricDescription description) {
        if (!srcToDstMetrics.containsKey(description)) {
            MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) description)
                    .getMetric(this);
            srcToDstMetrics.put(description, newMetric);
        }

        if (!dstToSrcMetrics.containsKey(description)) {
            MetricImpl newMetric = (MetricImpl) ((MetricDescriptionImpl) description)
                    .getMetric(this);
            dstToSrcMetrics.put(description, newMetric);
        }
    }

    @Override
    public void removeMetric(MetricDescription description) {
        srcToDstMetrics.remove(description);
        dstToSrcMetrics.remove(description);
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof LinkImpl))
            return false;

        // cast to native object is now safe
        LinkImpl that = (LinkImpl) thatObject;

        // now a proper field-by-field evaluation can be made
        return (this.source.equals(that.source) && this.destination
                .equals(that.destination))
                || (this.source.equals(that.destination) && this.destination
                        .equals(that.source));
    }

    public void setNotUpdated() {
        updatedThisCycle = false;
    }

    @Override
    public int hashCode() {
        int hashCode = source.hashCode() + destination.hashCode();
        return hashCode;
    }

    public String debugPrint() {
        String result = "";

        result += "link: bla" + "->" + " \n";

        result += "has " + srcToDstMetrics.size() + " metrics one way: \n";
        result += "has " + dstToSrcMetrics.size()
                + " metrics the other way: \n";

        for (Link child : children) {
            result += "  " + ((LinkImpl) child).debugPrint();
        }

        return result;
    }
}
