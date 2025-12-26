package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.model.NotificationEvent;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Filter notifications by API name.
 */
public class ApiFilter implements NotificationFilter {

    private final Set<String> includedApis;
    private final Set<String> excludedApis;
    private final List<Pattern> includePatterns;
    private final List<Pattern> excludePatterns;

    private ApiFilter(Builder builder) {
        this.includedApis = builder.includedApis != null
            ? new HashSet<>(builder.includedApis)
            : new HashSet<>();
        this.excludedApis = builder.excludedApis != null
            ? new HashSet<>(builder.excludedApis)
            : new HashSet<>();
        this.includePatterns = builder.includePatterns != null
            ? new ArrayList<>(builder.includePatterns)
            : new ArrayList<>();
        this.excludePatterns = builder.excludePatterns != null
            ? new ArrayList<>(builder.excludePatterns)
            : new ArrayList<>();
    }

    @Override
    public boolean shouldSend(Notification notification) {
        String apiName = getApiName(notification);
        if (apiName == null || apiName.isEmpty()) {
            // If no API name, allow by default unless we have specific includes
            return includedApis.isEmpty() && includePatterns.isEmpty();
        }

        // Check exclusions first
        if (excludedApis.contains(apiName)) {
            return false;
        }

        for (Pattern pattern : excludePatterns) {
            if (pattern.matcher(apiName).matches()) {
                return false;
            }
        }

        // If no includes specified, allow all non-excluded
        if (includedApis.isEmpty() && includePatterns.isEmpty()) {
            return true;
        }

        // Check includes
        if (includedApis.contains(apiName)) {
            return true;
        }

        for (Pattern pattern : includePatterns) {
            if (pattern.matcher(apiName).matches()) {
                return true;
            }
        }

        return false;
    }

    private String getApiName(Notification notification) {
        NotificationEvent event = notification.getEvent();
        if (event != null) {
            return event.getApiName();
        }
        return notification.getMetadata("apiName", String.class);
    }

    @Override
    public String getFilterReason(Notification notification) {
        String apiName = getApiName(notification);
        if (excludedApis.contains(apiName)) {
            return String.format("API '%s' is in excluded list", apiName);
        }
        return String.format("API '%s' not in allowed list", apiName);
    }

    @Override
    public int getPriority() {
        return 20;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a filter that includes only specified APIs.
     */
    public static ApiFilter include(String... apis) {
        return builder().include(apis).build();
    }

    /**
     * Create a filter that excludes specified APIs.
     */
    public static ApiFilter exclude(String... apis) {
        return builder().exclude(apis).build();
    }

    public static class Builder {
        private Set<String> includedApis;
        private Set<String> excludedApis;
        private List<Pattern> includePatterns;
        private List<Pattern> excludePatterns;

        public Builder include(String... apis) {
            if (this.includedApis == null) {
                this.includedApis = new HashSet<>();
            }
            this.includedApis.addAll(Arrays.asList(apis));
            return this;
        }

        public Builder exclude(String... apis) {
            if (this.excludedApis == null) {
                this.excludedApis = new HashSet<>();
            }
            this.excludedApis.addAll(Arrays.asList(apis));
            return this;
        }

        public Builder includePattern(String regex) {
            if (this.includePatterns == null) {
                this.includePatterns = new ArrayList<>();
            }
            this.includePatterns.add(Pattern.compile(regex));
            return this;
        }

        public Builder excludePattern(String regex) {
            if (this.excludePatterns == null) {
                this.excludePatterns = new ArrayList<>();
            }
            this.excludePatterns.add(Pattern.compile(regex));
            return this;
        }

        public ApiFilter build() {
            return new ApiFilter(this);
        }
    }
}
