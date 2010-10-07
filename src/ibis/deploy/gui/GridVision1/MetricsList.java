package ibis.deploy.gui.performance;

import ibis.deploy.gui.performance.metrics.Metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MetricsList implements List<Metric> {
	ArrayList<Metric> metrics;
	
	public MetricsList() {
		super();
		metrics = new ArrayList<Metric>();
	}

	@Override
	public boolean add(Metric newMetric) {
		return metrics.add(newMetric);
	}

	@Override
	public void add(int index, Metric newMetric) {
		metrics.add(index, newMetric);
	}

	@Override
	public boolean addAll(Collection<? extends Metric> c) {		
		return metrics.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Metric> c) {
		return metrics.addAll(index, c);
	}

	@Override
	public void clear() {
		metrics.clear();		
	}

	@Override
	public boolean contains(Object o) {		
		return metrics.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return metrics.containsAll(c);
	}

	@Override	
	public Metric get(int index) {
		return metrics.get(index);
	}

	@Override
	public int indexOf(Object o) {		
		return metrics.indexOf(o);
	}

	@Override
	public boolean isEmpty() {		
		return metrics.isEmpty();
	}

	@Override
	public Iterator<Metric> iterator() {		
		return metrics.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {		
		return metrics.lastIndexOf(o);
	}

	@Override
	public ListIterator<Metric> listIterator() {
		return metrics.listIterator();
	}

	@Override
	public ListIterator<Metric> listIterator(int index) {		
		return metrics.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return metrics.remove(o);
	}

	@Override
	public Metric remove(int index) {
		return metrics.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return metrics.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return metrics.retainAll(c);
	}

	@Override
	public Metric set(int index, Metric element) {
		return metrics.set(index, element);
	}

	@Override
	public int size() {
		return metrics.size();
	}

	@Override
	public List<Metric> subList(int fromIndex, int toIndex) {
		return metrics.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return metrics.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <MetricsObject> MetricsObject[] toArray(MetricsObject[] a) {
		return metrics.toArray(a);
	}

	public MetricsList clone() {
		MetricsList newList = new MetricsList();
		newList.addAll(metrics);
		return newList;
	}
}
