package io.github.mohmk10.changeloghub.core.comparator.impl;

import io.github.mohmk10.changeloghub.core.model.Change;
import io.github.mohmk10.changeloghub.core.model.ChangeCategory;
import io.github.mohmk10.changeloghub.core.model.ChangeType;
import io.github.mohmk10.changeloghub.core.model.Parameter;
import io.github.mohmk10.changeloghub.core.model.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParameterComparator {

    public List<Change> compare(List<Parameter> oldParams, List<Parameter> newParams) {
        List<Change> changes = new ArrayList<>();

        Map<String, Parameter> oldParamMap = toMap(oldParams);
        Map<String, Parameter> newParamMap = toMap(newParams);

        for (Parameter newParam : newParams) {
            Parameter oldParam = oldParamMap.get(newParam.getName());
            if (oldParam == null) {
                changes.add(createAddedParameterChange(newParam));
            } else {
                changes.addAll(compareParameters(oldParam, newParam));
            }
        }

        for (Parameter oldParam : oldParams) {
            if (!newParamMap.containsKey(oldParam.getName())) {
                changes.add(createRemovedParameterChange(oldParam));
            }
        }

        return changes;
    }

    private Map<String, Parameter> toMap(List<Parameter> params) {
        if (params == null) {
            return Map.of();
        }
        return params.stream()
                .collect(Collectors.toMap(Parameter::getName, Function.identity(), (a, b) -> a));
    }

    private Change createAddedParameterChange(Parameter param) {
        Severity severity = param.isRequired() ? Severity.BREAKING : Severity.INFO;
        String description = param.isRequired()
                ? "Required parameter '" + param.getName() + "' added"
                : "Optional parameter '" + param.getName() + "' added";

        return Change.builder()
                .type(ChangeType.ADDED)
                .category(ChangeCategory.PARAMETER)
                .severity(severity)
                .path("parameter:" + param.getName())
                .description(description)
                .oldValue(null)
                .newValue(param)
                .build();
    }

    private Change createRemovedParameterChange(Parameter param) {
        return Change.builder()
                .type(ChangeType.REMOVED)
                .category(ChangeCategory.PARAMETER)
                .severity(Severity.DANGEROUS)
                .path("parameter:" + param.getName())
                .description("Parameter '" + param.getName() + "' removed")
                .oldValue(param)
                .newValue(null)
                .build();
    }

    private List<Change> compareParameters(Parameter oldParam, Parameter newParam) {
        List<Change> changes = new ArrayList<>();

        if (!Objects.equals(oldParam.getType(), newParam.getType())) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.PARAMETER)
                    .severity(Severity.BREAKING)
                    .path("parameter:" + newParam.getName() + ".type")
                    .description("Parameter '" + newParam.getName() + "' type changed from '"
                            + oldParam.getType() + "' to '" + newParam.getType() + "'")
                    .oldValue(oldParam.getType())
                    .newValue(newParam.getType())
                    .build());
        }

        if (!oldParam.isRequired() && newParam.isRequired()) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.PARAMETER)
                    .severity(Severity.BREAKING)
                    .path("parameter:" + newParam.getName() + ".required")
                    .description("Parameter '" + newParam.getName() + "' changed from optional to required")
                    .oldValue(false)
                    .newValue(true)
                    .build());
        } else if (oldParam.isRequired() && !newParam.isRequired()) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.PARAMETER)
                    .severity(Severity.INFO)
                    .path("parameter:" + newParam.getName() + ".required")
                    .description("Parameter '" + newParam.getName() + "' changed from required to optional")
                    .oldValue(true)
                    .newValue(false)
                    .build());
        }

        if (oldParam.getLocation() != newParam.getLocation()) {
            changes.add(Change.builder()
                    .type(ChangeType.MODIFIED)
                    .category(ChangeCategory.PARAMETER)
                    .severity(Severity.BREAKING)
                    .path("parameter:" + newParam.getName() + ".location")
                    .description("Parameter '" + newParam.getName() + "' location changed from "
                            + oldParam.getLocation() + " to " + newParam.getLocation())
                    .oldValue(oldParam.getLocation())
                    .newValue(newParam.getLocation())
                    .build());
        }

        return changes;
    }
}
