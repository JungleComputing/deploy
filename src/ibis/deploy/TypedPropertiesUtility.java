package ibis.deploy;

import ibis.util.TypedProperties;

class TypedPropertiesUtility {

	protected static String getHierarchicalProperty(TypedProperties properties,
			String hierarchicalPart, String specificPart, String defaultValue) {
		if (specificPart == null) {
			return null;
		}
		if (hierarchicalPart == null) {
			hierarchicalPart = "";
		}
		do {
			String prefix = "";
			if (!hierarchicalPart.equals("")) {
				prefix += hierarchicalPart + ".";
			}
			if (properties.getProperty(prefix + specificPart) != null) {
				return properties.getProperty(prefix + specificPart);
			} else {
				int lastIndexOfDot = hierarchicalPart.lastIndexOf(".");
				if (lastIndexOfDot == -1 && hierarchicalPart.equals("")) {
					break;
				} else if (lastIndexOfDot == -1) {
					hierarchicalPart = "";
				} else {
					hierarchicalPart = hierarchicalPart.substring(0,
							lastIndexOfDot);
				}
			}

		} while (true);
		return defaultValue;
	}

	protected static String[] getHierarchicalStringList(
			TypedProperties properties, String hierarchicalPart,
			String specificPart, String[] defaultValue, String separator) {
		String result = getHierarchicalProperty(properties, hierarchicalPart,
				specificPart, null);
		if (result == null) {
			return defaultValue;
		} else {
			return result.split(separator);
		}
	}

	protected static int getHierarchicalInt(TypedProperties properties,
			String hierarchicalPart, String specificPart, int defaultValue) {
		String result = getHierarchicalProperty(properties, hierarchicalPart,
				specificPart, null);
		if (result == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(result);
		}
	}

	protected static boolean getHierarchicalBoolean(TypedProperties properties,
			String hierarchicalPart, String specificPart, boolean defaultValue) {
		String result = getHierarchicalProperty(properties, hierarchicalPart,
				specificPart, null);
		if (result == null) {
			return defaultValue;
		} else {
			return Boolean.parseBoolean(result);
		}
	}

}
