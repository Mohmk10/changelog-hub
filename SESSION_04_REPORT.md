# Session 04 - Rapport

## Objectifs
- [x] Configurer maven-plugin/pom.xml
- [x] Créer AbstractChangelogMojo (classe de base)
- [x] Implémenter CompareMojo
- [x] Implémenter AnalyzeMojo
- [x] Implémenter ValidateMojo
- [x] Implémenter DetectBreakingChangesMojo
- [x] Créer tests unitaires
- [x] Créer README documentation

## Statistiques

### Module Maven Plugin
| Élément | Quantité |
|---------|----------|
| Goals | 4 |
| Classes | 5 |
| Tests | 45 |

### Goals disponibles
| Goal | Description | Default Phase | Fail on Breaking |
|------|-------------|---------------|------------------|
| changelog:compare | Compare 2 specs API | verify | false |
| changelog:analyze | Analyse une spec | verify | - |
| changelog:validate | Valide une spec | validate | true (on error) |
| changelog:detect | Détecte breaking changes (CI/CD) | verify | true |

### Paramètres communs
| Paramètre | Property | Default | Description |
|-----------|----------|---------|-------------|
| skip | changelog.skip | false | Skip execution |
| verbose | changelog.verbose | false | Verbose output |
| format | changelog.format | console | Output format |
| outputDirectory | changelog.outputDirectory | ${project.build.directory}/changelog | Output dir |

### Tests totaux projet
| Module | Tests |
|--------|-------|
| core | 130 |
| openapi-parser | 79 |
| cli | 27 |
| maven-plugin | 45 |
| **Total** | **281** |

## Utilisation Maven Plugin

### Installation
```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.mohmk10</groupId>
            <artifactId>changelog-hub-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

### Exemples
```bash
# Comparer deux versions d'API
mvn changelog:compare \
  -Dchangelog.oldSpec=api-v1.yaml \
  -Dchangelog.newSpec=api-v2.yaml

# Générer un changelog Markdown
mvn changelog:compare \
  -Dchangelog.oldSpec=old.yaml \
  -Dchangelog.newSpec=new.yaml \
  -Dchangelog.format=markdown \
  -Dchangelog.outputFile=CHANGELOG.md

# Détecter breaking changes (CI/CD)
mvn changelog:detect \
  -Dchangelog.oldSpec=old.yaml \
  -Dchangelog.newSpec=new.yaml

# Analyser une API
mvn changelog:analyze -Dchangelog.spec=api.yaml

# Valider une spécification
mvn changelog:validate -Dchangelog.spec=api.yaml -Dchangelog.strict=true
```

### Intégration Build Lifecycle
```xml
<plugin>
    <groupId>io.github.mohmk10</groupId>
    <artifactId>changelog-hub-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>detect-breaking-changes</id>
            <phase>verify</phase>
            <goals>
                <goal>detect</goal>
            </goals>
            <configuration>
                <oldSpec>${project.basedir}/api-v1.yaml</oldSpec>
                <newSpec>${project.basedir}/api.yaml</newSpec>
                <failOnBreaking>true</failOnBreaking>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Modules complétés
1. ✅ core - Moteur de détection (130 tests)
2. ✅ openapi-parser - Parser OpenAPI/Swagger (79 tests)
3. ✅ cli - Interface ligne de commande (27 tests)
4. ✅ maven-plugin - Plugin Maven (45 tests)

## Prochaine Session (05)
Options possibles :
- spring-parser : Parser annotations Spring Boot (@GetMapping, @PostMapping, etc.)
- graphql-parser : Parser schemas GraphQL
- gradle-plugin : Plugin Gradle équivalent

## Commandes utiles
```bash
# Build complet
mvn clean install

# Build Maven Plugin uniquement
mvn clean install -pl maven-plugin -am

# Tests Maven Plugin
mvn clean test -pl maven-plugin

# Afficher aide du plugin
mvn io.github.mohmk10:changelog-hub-maven-plugin:1.0.0-SNAPSHOT:help
```

---
Generated: 2025-12-25
Changelog Hub v1.0.0-SNAPSHOT
