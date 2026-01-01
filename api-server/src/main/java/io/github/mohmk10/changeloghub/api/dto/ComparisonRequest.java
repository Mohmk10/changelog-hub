package io.github.mohmk10.changeloghub.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ComparisonRequest {

    @NotBlank(message = "Old spec content is required")
    private String oldSpec;

    @NotBlank(message = "New spec content is required")
    private String newSpec;

    private String oldSpecName;
    private String newSpecName;
    private String format;

    public ComparisonRequest() {}

    public String getOldSpec() { return oldSpec; }
    public void setOldSpec(String oldSpec) { this.oldSpec = oldSpec; }

    public String getNewSpec() { return newSpec; }
    public void setNewSpec(String newSpec) { this.newSpec = newSpec; }

    public String getOldSpecName() { return oldSpecName; }
    public void setOldSpecName(String oldSpecName) { this.oldSpecName = oldSpecName; }

    public String getNewSpecName() { return newSpecName; }
    public void setNewSpecName(String newSpecName) { this.newSpecName = newSpecName; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}
