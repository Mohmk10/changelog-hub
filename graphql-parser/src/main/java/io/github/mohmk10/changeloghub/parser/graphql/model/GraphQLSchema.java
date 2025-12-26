package io.github.mohmk10.changeloghub.parser.graphql.model;

import java.util.*;

public class GraphQLSchema {

    private String name;
    private String version;
    private String description;
    private String sourceFile;
    private Map<String, GraphQLType> types = new LinkedHashMap<>();
    private List<GraphQLOperation> queries = new ArrayList<>();
    private List<GraphQLOperation> mutations = new ArrayList<>();
    private List<GraphQLOperation> subscriptions = new ArrayList<>();
    private List<String> directives = new ArrayList<>();

    public GraphQLSchema() {
    }

    public GraphQLSchema(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Map<String, GraphQLType> getTypes() {
        return types;
    }

    public void setTypes(Map<String, GraphQLType> types) {
        this.types = types != null ? new LinkedHashMap<>(types) : new LinkedHashMap<>();
    }

    public void addType(GraphQLType type) {
        this.types.put(type.getName(), type);
    }

    public Optional<GraphQLType> getType(String name) {
        return Optional.ofNullable(types.get(name));
    }

    public List<GraphQLOperation> getQueries() {
        return queries;
    }

    public void setQueries(List<GraphQLOperation> queries) {
        this.queries = queries != null ? new ArrayList<>(queries) : new ArrayList<>();
    }

    public void addQuery(GraphQLOperation query) {
        this.queries.add(query);
    }

    public List<GraphQLOperation> getMutations() {
        return mutations;
    }

    public void setMutations(List<GraphQLOperation> mutations) {
        this.mutations = mutations != null ? new ArrayList<>(mutations) : new ArrayList<>();
    }

    public void addMutation(GraphQLOperation mutation) {
        this.mutations.add(mutation);
    }

    public List<GraphQLOperation> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<GraphQLOperation> subscriptions) {
        this.subscriptions = subscriptions != null ? new ArrayList<>(subscriptions) : new ArrayList<>();
    }

    public void addSubscription(GraphQLOperation subscription) {
        this.subscriptions.add(subscription);
    }

    public List<String> getDirectives() {
        return directives;
    }

    public void setDirectives(List<String> directives) {
        this.directives = directives != null ? new ArrayList<>(directives) : new ArrayList<>();
    }

    public void addDirective(String directive) {
        this.directives.add(directive);
    }

    public List<GraphQLOperation> getAllOperations() {
        List<GraphQLOperation> all = new ArrayList<>();
        all.addAll(queries);
        all.addAll(mutations);
        all.addAll(subscriptions);
        return all;
    }

    public int getOperationCount() {
        return queries.size() + mutations.size() + subscriptions.size();
    }

    public int getTypeCount() {
        return types.size();
    }

    @Override
    public String toString() {
        return "GraphQLSchema{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", types=" + types.size() +
                ", queries=" + queries.size() +
                ", mutations=" + mutations.size() +
                ", subscriptions=" + subscriptions.size() +
                '}';
    }
}
