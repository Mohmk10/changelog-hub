package io.github.mohmk10.changeloghub.git.analyzer;

import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.model.GitTag;
import io.github.mohmk10.changeloghub.git.util.GitConstants;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class TagAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(TagAnalyzer.class);

    private final Repository repository;
    private final GitConfig config;

    public TagAnalyzer(Repository repository) {
        this(repository, new GitConfig());
    }

    public TagAnalyzer(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
    }

    public List<GitTag> getAllTags() {
        List<GitTag> tags = new ArrayList<>();

        try (Git git = new Git(repository)) {
            List<Ref> tagRefs = git.tagList().call();

            try (RevWalk revWalk = new RevWalk(repository)) {
                for (Ref ref : tagRefs) {
                    GitTag tag = parseTag(ref, revWalk);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get tags", e);
        }

        return tags;
    }

    public Optional<GitTag> getTag(String tagName) {
        try {
            String fullTagName = tagName.startsWith("refs/tags/")
                ? tagName
                : "refs/tags/" + tagName;

            Ref tagRef = repository.findRef(fullTagName);
            if (tagRef == null) {
                return Optional.empty();
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                return Optional.ofNullable(parseTag(tagRef, revWalk));
            }
        } catch (Exception e) {
            logger.error("Failed to get tag: {}", tagName, e);
            return Optional.empty();
        }
    }

    public List<GitTag> getSemanticVersionTags() {
        return getAllTags().stream()
            .filter(GitTag::isSemanticVersion)
            .collect(Collectors.toList());
    }

    public List<GitTag> getTagsSortedByVersion() {
        return getSemanticVersionTags().stream()
            .sorted((t1, t2) -> compareVersions(t2.getVersion(), t1.getVersion()))
            .collect(Collectors.toList());
    }

    public List<GitTag> getTagsSortedByDate() {
        return getAllTags().stream()
            .filter(t -> t.getDate() != null)
            .sorted(Comparator.comparing(GitTag::getDate).reversed())
            .collect(Collectors.toList());
    }

    public Optional<GitTag> getLatestTag() {
        List<GitTag> sorted = getTagsSortedByVersion();
        return sorted.isEmpty() ? Optional.empty() : Optional.of(sorted.get(0));
    }

    public Optional<GitTag> getLatestTagByDate() {
        List<GitTag> sorted = getTagsSortedByDate();
        return sorted.isEmpty() ? Optional.empty() : Optional.of(sorted.get(0));
    }

    public List<GitTag> getTagsBetweenVersions(String fromVersion, String toVersion) {
        List<GitTag> allTags = getTagsSortedByVersion();
        List<GitTag> result = new ArrayList<>();

        boolean inRange = false;
        for (GitTag tag : allTags) {
            String version = tag.getVersion();

            if (version.equals(toVersion)) {
                inRange = true;
            }

            if (inRange) {
                result.add(tag);
            }

            if (version.equals(fromVersion)) {
                break;
            }
        }

        return result;
    }

    public Optional<GitTag> getPreviousTag(String tagName) {
        List<GitTag> sorted = getTagsSortedByVersion();
        String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getVersion().equals(version) && i + 1 < sorted.size()) {
                return Optional.of(sorted.get(i + 1));
            }
        }

        return Optional.empty();
    }

    public Optional<GitTag> getNextTag(String tagName) {
        List<GitTag> sorted = getTagsSortedByVersion();
        String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getVersion().equals(version) && i > 0) {
                return Optional.of(sorted.get(i - 1));
            }
        }

        return Optional.empty();
    }

    public List<GitTag> filterByMajorVersion(int major) {
        return getSemanticVersionTags().stream()
            .filter(tag -> {
                int[] parts = parseVersionParts(tag.getVersion());
                return parts[0] == major;
            })
            .collect(Collectors.toList());
    }

    public Map<Integer, List<GitTag>> groupByMajorVersion() {
        return getSemanticVersionTags().stream()
            .collect(Collectors.groupingBy(
                tag -> parseVersionParts(tag.getVersion())[0],
                TreeMap::new,
                Collectors.toList()
            ));
    }

    public boolean tagExists(String tagName) {
        return getTag(tagName).isPresent();
    }

    public List<GitTag> getAnnotatedTags() {
        return getAllTags().stream()
            .filter(GitTag::isAnnotated)
            .collect(Collectors.toList());
    }

    public List<GitTag> getLightweightTags() {
        return getAllTags().stream()
            .filter(GitTag::isLightweight)
            .collect(Collectors.toList());
    }

    private GitTag parseTag(Ref ref, RevWalk revWalk) {
        try {
            ObjectId objectId = ref.getPeeledObjectId();
            if (objectId == null) {
                objectId = ref.getObjectId();
            }

            RevObject revObject = revWalk.parseAny(ref.getObjectId());
            boolean annotated = revObject instanceof RevTag;

            GitTag.Builder builder = GitTag.builder()
                .name(ref.getName().replace("refs/tags/", ""))
                .commitId(objectId.getName())
                .annotated(annotated);

            if (annotated) {
                RevTag revTag = (RevTag) revObject;
                builder.message(revTag.getFullMessage());
                if (revTag.getTaggerIdent() != null) {
                    builder.tagger(revTag.getTaggerIdent().getName());
                    builder.taggerEmail(revTag.getTaggerIdent().getEmailAddress());
                    builder.date(LocalDateTime.ofInstant(
                        revTag.getTaggerIdent().getWhen().toInstant(),
                        ZoneId.systemDefault()
                    ));
                }
            } else {
                
                RevCommit commit = revWalk.parseCommit(objectId);
                builder.date(LocalDateTime.ofInstant(
                    commit.getAuthorIdent().getWhen().toInstant(),
                    ZoneId.systemDefault()
                ));
            }

            return builder.build();

        } catch (Exception e) {
            logger.warn("Failed to parse tag: {}", ref.getName(), e);
            return null;
        }
    }

    private int compareVersions(String v1, String v2) {
        int[] parts1 = parseVersionParts(v1);
        int[] parts2 = parseVersionParts(v2);

        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            int p1 = i < parts1.length ? parts1[i] : 0;
            int p2 = i < parts2.length ? parts2[i] : 0;

            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }

        return 0;
    }

    private int[] parseVersionParts(String version) {
        if (version == null) return new int[]{0, 0, 0};

        Matcher matcher = GitConstants.SEMANTIC_VERSION_PATTERN.matcher(version);
        if (!matcher.matches()) {
            return new int[]{0, 0, 0};
        }

        try {
            return new int[]{
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
            };
        } catch (NumberFormatException e) {
            return new int[]{0, 0, 0};
        }
    }
}
