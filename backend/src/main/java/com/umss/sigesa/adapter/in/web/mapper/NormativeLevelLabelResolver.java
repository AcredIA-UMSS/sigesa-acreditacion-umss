package com.umss.sigesa.adapter.in.web.mapper;

public final class NormativeLevelLabelResolver {

    private NormativeLevelLabelResolver() {
    }

    public static String level1Label(String evaluatorModel) {
        return isArcuSur(evaluatorModel) ? "Dimensión" : "Área";
    }

    public static String level2Label(String evaluatorModel) {
        return isArcuSur(evaluatorModel) ? "Componente" : "Variable";
    }

    public static String level3Label(String evaluatorModel) {
        return isArcuSur(evaluatorModel) ? "Criterio" : "Sub-variable";
    }

    private static boolean isArcuSur(String evaluatorModel) {
        return evaluatorModel != null && evaluatorModel.toUpperCase().contains("ARCU");
    }
}
