# 🧪 TDD en Java — Exercices Jour 1
### Définition, Cycle TDD, JUnit 5, Bonnes Pratiques Agiles

---

## 📋 Sommaire

- [Niveau 1 — Découverte (débutant)](#niveau-1--découverte-débutant)
  - [Ex 1.1 — Premier test JUnit 5](#ex-11--premier-test-junit-5)
  - [Ex 1.2 — Le cycle Red-Green-Refactor](#ex-12--le-cycle-red-green-refactor)
  - [Ex 1.3 — Pattern AAA](#ex-13--pattern-aaa)
  - [Ex 1.4 — Tester les exceptions](#ex-14--tester-les-exceptions)
- [Niveau 2 — Application (intermédiaire)](#niveau-2--application-intermédiaire)
  - [Ex 2.1 — Compte bancaire en TDD](#ex-21--compte-bancaire-en-tdd)
  - [Ex 2.2 — Tests paramétrés](#ex-22--tests-paramétrés)
  - [Ex 2.3 — Organisation avec @Nested](#ex-23--organisation-avec-nested)
  - [Ex 2.4 — Cas limites et valeurs frontières](#ex-24--cas-limites-et-valeurs-frontières)
- [Niveau 3 — Consolidation (avancé)](#niveau-3--consolidation-avancé)
  - [Ex 3.1 — Calculatrice de factures complète](#ex-31--calculatrice-de-factures-complète)
  - [Ex 3.2 — Refactoring guidé par les tests](#ex-32--refactoring-guidé-par-les-tests)
  - [Ex 3.3 — TDD avec Repository Pattern](#ex-33--tdd-avec-repository-pattern)
  - [Ex 3.4 — Couverture JaCoCo](#ex-34--couverture-jacoco)
- [Niveau 4 — Projet intégré](#niveau-4--projet-intégré)
  - [Ex 4.1 — Gestionnaire de tâches (TodoList)](#ex-41--gestionnaire-de-tâches-todolist)
- [Corrigés partiels](#corrigés-partiels)

---

## Niveau 1 — Découverte (débutant)

> **Objectif :** Prendre en main JUnit 5, comprendre le cycle TDD et le patron AAA.
> **Durée estimée :** 1h30

---

### Ex 1.1 — Premier test JUnit 5

**Contexte :** Vous devez implémenter une classe `Calculatrice` capable d'additionner deux entiers.

**Étape 1 — Setup du projet Maven**

Créez un projet Maven avec la structure suivante et le `pom.xml` ci-dessous :

```
tp-jour1/
├── src/
│   ├── main/java/com/formation/tdd/
│   │   └── Calculatrice.java          ← à créer APRÈS les tests
│   └── test/java/com/formation/tdd/
│       └── CalculatriceTest.java      ← à créer EN PREMIER
└── pom.xml
```

```xml
<!-- pom.xml -->
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.formation</groupId>
  <artifactId>tp-jour1</artifactId>
  <version>1.0-SNAPSHOT</version>

  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.1.2</version>
      </plugin>
    </plugins>
  </build>
</project>
```

**Étape 2 — Écrire le test (RED)**

Écrivez dans `CalculatriceTest.java` un test qui vérifie que `2 + 3 = 5`.

```java
// CalculatriceTest.java
package com.formation.tdd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatriceTest {

    @Test
    void additionner_DeuxEntiers_RetourneLaSomme() {
        // À compléter en suivant le patron AAA
        // Arrange : ...
        // Act : ...
        // Assert : ...
    }
}
```

**Étape 3 — Faire passer le test (GREEN)**

Créez `Calculatrice.java` avec le **minimum de code** nécessaire.

**Étape 4 — Refactorer (REFACTOR)**

Améliorez les noms de variables et la clarté du code sans changer le comportement.

**Questions de réflexion :**

1. Pourquoi avez-vous vu une erreur de compilation avant d'écrire `Calculatrice.java` ?
2. Pourquoi le test était-il rouge avant votre implémentation ?
3. Qu'auriez-vous pu améliorer lors du refactoring ?

---

### Ex 1.2 — Le cycle Red-Green-Refactor

**Contexte :** Implémentez la soustraction, la multiplication et la division en respectant **strictement** le cycle TDD : un test à la fois.

**Règles absolues à respecter :**

- ✅ Écrire **un seul test** → le voir échouer → implémenter → le voir passer → refactorer
- ❌ Ne jamais écrire deux tests avant d'avoir fait passer le premier
- ❌ Ne jamais écrire plus de code que nécessaire pour faire passer le test

**Tests à implémenter un par un dans cet ordre :**

```
Test 1 : soustraire(5, 3)  → 2
Test 2 : soustraire(0, 5)  → -5
Test 3 : multiplier(4, 3)  → 12
Test 4 : multiplier(5, 0)  → 0
Test 5 : diviser(10, 2)    → 5.0
Test 6 : diviser(10, 0)    → lève ArithmeticException
```

**Pour chaque test, notez :**

| Test | Temps RED | Temps GREEN | Temps REFACTOR | Observation |
|------|-----------|-------------|----------------|-------------|
| soustraire(5,3) | | | | |
| soustraire(0,5) | | | | |
| multiplier(4,3) | | | | |
| multiplier(5,0) | | | | |
| diviser(10,2)   | | | | |
| diviser(10,0)   | | | | |

---

### Ex 1.3 — Pattern AAA

**Contexte :** Pour chaque test suivant, réécrivez-le en respectant **strictement** le pattern Arrange / Act / Assert avec des commentaires explicites.

**Tests à réécrire (version mélangée à corriger) :**

```java
// ❌ Version mélangée — à réécrire proprement
@Test
void test1() {
    assertEquals(3, new Calculatrice().additionner(1, 2));
}

@Test
void test2() {
    Calculatrice c = new Calculatrice();
    int r = c.multiplier(3, 4);
    c.additionner(1, 1); // ligne inutile
    assertEquals(12, r);
    assertEquals(2, c.additionner(1, 1)); // deux assertions non liées
}

@Test
void test3() {
    // Pas de commentaires, nommage cryptique
    Calculatrice x = new Calculatrice();
    assertEquals(-5.0, x.diviser(-10, 2), 0.001);
    assertThrows(ArithmeticException.class, () -> x.diviser(5, 0));
}
```

**Critères de réussite :**

- Chaque test a exactement **trois blocs** commentés `// Arrange`, `// Act`, `// Assert`
- Chaque test vérifie **une seule chose**
- Le nom du test suit le pattern `methode_Contexte_ResultatAttendu`

---

### Ex 1.4 — Tester les exceptions

**Contexte :** Implémentez une classe `Validateur` et ses tests d'exception.

**Spécifications métier :**

- `validerAge(int age)` → lève `IllegalArgumentException` si age < 0 ou age > 150
- `validerEmail(String email)` → lève `IllegalArgumentException` si null, vide, ou sans `@`
- `validerMontant(double montant)` → lève `IllegalArgumentException` si montant < 0

**Complétez les tests suivants :**

```java
class ValidateurTest {

    private Validateur validateur;

    @BeforeEach
    void setUp() {
        validateur = new Validateur();
    }

    @Test
    void validerAge_AgeNegatif_LeveIllegalArgumentException() {
        // TODO : utiliser assertThrows
    }

    @Test
    void validerAge_AgeSuperieura150_LeveIllegalArgumentException() {
        // TODO
    }

    @Test
    void validerAge_AgeValide_PasException() {
        // TODO : utiliser assertDoesNotThrow
    }

    @Test
    void validerEmail_EmailNull_LeveException() {
        // TODO
    }

    @Test
    void validerEmail_EmailSansArobase_LeveException() {
        // TODO : vérifier AUSSI le message de l'exception
    }

    @Test
    void validerMontant_MontantNegatif_LeveExceptionAvecMessage() {
        // TODO : capturer l'exception ET vérifier son message
        // Le message doit contenir le montant invalide
    }
}
```

**Bonus :** Vérifiez que le message de chaque exception contient la valeur invalide.

---

## Niveau 2 — Application (intermédiaire)

> **Objectif :** Pratiquer le TDD sur des cas réels plus complets. Découvrir les fonctionnalités avancées de JUnit 5.
> **Durée estimée :** 2h00

---

### Ex 2.1 — Compte bancaire en TDD

**Contexte :** Implémentez un compte bancaire **entièrement en TDD**. Écrivez chaque test avant son implémentation.

**Spécifications (à transformer en tests) :**

```
RÈGLES MÉTIER :
✅ Un compte est créé avec un solde initial (peut être 0)
✅ On peut déposer un montant positif → le solde augmente
✅ On peut retirer un montant si le solde est suffisant → le solde diminue
✅ Un retrait impossible lève SoldeInsuffisantException
✅ Un dépôt ou retrait de montant négatif ou nul lève IllegalArgumentException
✅ Le solde ne peut jamais être négatif
✅ On peut consulter l'historique des opérations
✅ L'historique contient le type (DEPOT/RETRAIT), le montant et le solde après
```

**Plan de développement TDD — dans cet ordre :**

```
1. creerCompte_SoldeInitialZero_SoldeEgalZero
2. creerCompte_SoldeInitialPositif_SoldeCorrect
3. creerCompte_SoldeInitialNegatif_LeveException
4. deposer_MontantPositif_AugmenteSolde
5. deposer_MontantNul_LeveException
6. deposer_MontantNegatif_LeveException
7. retirer_MontantValide_DiminueSolde
8. retirer_MontantEgalSolde_SoldeZero
9. retirer_MontantSuperieurSolde_LeveSoldeInsuffisantException
10. retirer_MontantNul_LeveException
11. historique_ApresDépôt_ContientOperation
12. historique_AprèsRetrait_ContientOperation
13. historique_PlusieursOperations_DansOrdreChronologique
```

**Classes à créer :**

```java
// À implémenter en TDD
public class CompteBancaire {
    // ...
}

public class Operation {
    private TypeOperation type; // DEPOT ou RETRAIT
    private double montant;
    private double soldeApres;
    // ...
}

public enum TypeOperation { DEPOT, RETRAIT }

public class SoldeInsuffisantException extends RuntimeException {
    // ...
}
```

---

### Ex 2.2 — Tests paramétrés

**Contexte :** Transformez les tests répétitifs en tests paramétrés `@ParameterizedTest`.

**Situation de départ — tests répétitifs :**

```java
// ❌ Duplication — à transformer en tests paramétrés
@Test
void calculerTVA_Taux20_Montant100_Retourne20() {
    assertEquals(20.0, calc.calculer(100.0, 0.20), 0.001);
}

@Test
void calculerTVA_Taux10_Montant200_Retourne20() {
    assertEquals(20.0, calc.calculer(200.0, 0.10), 0.001);
}

@Test
void calculerTVA_Taux55_Montant500_Retourne27_5() {
    assertEquals(27.5, calc.calculer(500.0, 0.055), 0.001);
}

@Test
void deposer_MontantNegatif1_LeveException() {
    assertThrows(IllegalArgumentException.class, () -> compte.deposer(-1));
}

@Test
void deposer_MontantNegatif100_LeveException() {
    assertThrows(IllegalArgumentException.class, () -> compte.deposer(-100));
}

@Test
void deposer_MontantNul_LeveException() {
    assertThrows(IllegalArgumentException.class, () -> compte.deposer(0));
}
```

**À faire :**

1. Transformez le premier groupe en `@CsvSource`
2. Transformez le second groupe en `@ValueSource`
3. Créez un `@MethodSource` fournissant des `Arguments` complexes pour tester l'historique du compte bancaire (type d'opération, montant, solde attendu)

---

### Ex 2.3 — Organisation avec @Nested

**Contexte :** Réorganisez la suite de tests du compte bancaire (Ex 2.1) avec `@Nested`.

**Structure cible :**

```java
@DisplayName("Compte Bancaire")
class CompteBancaireTest {

    @Nested
    @DisplayName("Création du compte")
    class CreationTest {
        // Tests de création (3 tests)
    }

    @Nested
    @DisplayName("Opérations de dépôt")
    class DepotTest {
        // Tests de dépôt (3 tests)
    }

    @Nested
    @DisplayName("Opérations de retrait")
    class RetraitTest {
        // Tests de retrait (4 tests)
    }

    @Nested
    @DisplayName("Historique des opérations")
    class HistoriqueTest {
        // Tests d'historique (3 tests)
    }
}
```

**Contraintes :**

- Chaque classe imbriquée a son propre `@BeforeEach` si nécessaire
- Les fixtures partagées sont dans la classe parente
- Utilisez `@DisplayName` pour tous les tests

---

### Ex 2.4 — Cas limites et valeurs frontières

**Contexte :** Appliquez la technique des **valeurs frontières** sur une classe `Remise`.

**Spécifications :**

```
BARÈME DES REMISES :
  Montant < 100      → 0% de remise
  100 ≤ Montant < 500  → 5% de remise
  500 ≤ Montant < 1000 → 10% de remise
  Montant ≥ 1000     → 15% de remise
  Montant < 0        → IllegalArgumentException
```

**Identifiez toutes les valeurs frontières et écrivez un test pour chacune :**

| Valeur testée | Remise attendue | Justification |
|---------------|-----------------|---------------|
| -0.01 | Exception | En-dessous du minimum |
| 0.0 | 0% | Minimum absolu |
| 99.99 | 0% | Juste avant première frontière |
| 100.0 | 5% | Exactement à la première frontière |
| 100.01 | 5% | Juste après la première frontière |
| ... | ... | ... |

**À compléter** : identifiez et testez les **8 valeurs frontières** restantes.

---

## Niveau 3 — Consolidation (avancé)

> **Objectif :** Développer un composant complet en TDD pur, maîtriser le refactoring et les outils de couverture.
> **Durée estimée :** 2h30

---

### Ex 3.1 — Calculatrice de factures complète

**Contexte :** Implémentez une calculatrice de factures intégrant TVA, remises et arrondis, **entièrement en TDD**.

**Spécifications métier complètes :**

```
CALCUL DE FACTURE :
  - Montant HT obligatoire, strictement positif
  - Taux TVA parmi : 0.0%, 5.5%, 10.0%, 20.0%
  - Remise possible sur le HT (0 à 50%, inclusive)
  - Formule : TTC = HT × (1 - remise) × (1 + TVA)
  - Résultat arrondi à 2 décimales (arrondi bancaire)
  - Un devis peut contenir plusieurs lignes de facture
  - Le total du devis = somme des TTC de chaque ligne
```

**Plan TDD suggéré :**

```
SPRINT 1 — Ligne simple
  1. ligne_HTValide_TauxZero_TTCegalHT
  2. ligne_HTValide_Taux20_TTCCorrect
  3. ligne_HTNegatif_LeveException
  4. ligne_TauxInvalide_LeveException

SPRINT 2 — Remises
  5. ligne_RemiseZero_TTCsansRemise
  6. ligne_Remise10pct_TTCavecRemise
  7. ligne_RemiseSuperieure50pct_LeveException
  8. ligne_RemiseNegative_LeveException

SPRINT 3 — Arrondis
  9. ligne_MontantGenerantDecimalesMultiples_ArrondiA2Decimales

SPRINT 4 — Devis multi-lignes
  10. devis_UneLigne_TotalEgalTTC
  11. devis_DeuxLignes_TotalEgalSomme
  12. devis_AucuneLigne_LeveException
  13. devis_TotalArrondi_A2Decimales
```

**Interface attendue :**

```java
public class LigneFacture {
    public LigneFacture(double montantHT, TauxTVA taux, double remise) {}
    public double calculerTTC() {}
}

public enum TauxTVA {
    ZERO(0.0), REDUIT(0.055), INTERMEDIAIRE(0.10), NORMAL(0.20);
    private final double valeur;
}

public class Devis {
    public void ajouterLigne(LigneFacture ligne) {}
    public double calculerTotal() {}
}
```

---

### Ex 3.2 — Refactoring guidé par les tests

**Contexte :** Vous héritez du code ci-dessous. Vos tests de caractérisation doivent vous permettre de le refactorer en toute sécurité.

**Code legacy fourni (ne pas modifier avant d'avoir les tests) :**

```java
public class Calc {
    public double calc(double a, String t, double r) {
        double x = 0;
        if (t.equals("TVA_NORM")) x = 0.20;
        else if (t.equals("TVA_RED")) x = 0.055;
        else if (t.equals("TVA_INT")) x = 0.10;
        else if (t.equals("TVA_ZERO")) x = 0;
        else throw new RuntimeException("bad");
        if (r < 0 || r > 0.5) throw new RuntimeException("bad remise");
        double b = a * (1 - r);
        double c = b * (1 + x);
        return Math.round(c * 100.0) / 100.0;
    }
}
```

**Étape 1 — Tests de caractérisation**

Écrivez des tests qui documentent le comportement **actuel** (sans comprendre si c'est correct).

**Étape 2 — Refactoring**

En gardant les tests verts à chaque étape :

1. Renommer la méthode et les paramètres
2. Extraire les constantes nommées pour les taux
3. Remplacer la chaîne de if/else par un enum
4. Extraire des méthodes privées (`calculerRemise`, `calculerTVA`)
5. Améliorer les messages d'exception

**Règle :** Relancez les tests après **chaque** modification.

---

### Ex 3.3 — TDD avec Repository Pattern

**Contexte :** Implémentez un service de gestion d'utilisateurs en TDD, en isolant l'accès aux données via le pattern Repository.

**Objectif :** Tous les tests doivent être **rapides** (< 50ms) sans accès BDD réelle.

**Interfaces à définir :**

```java
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsByEmail(String email);
}

public class UserService {
    public UserService(UserRepository repository) {}

    public User inscrire(String nom, String email, String motDePasse) {}
    public User connecter(String email, String motDePasse) {}
    public void desactiver(Long userId) {}
    public List<User> listerActifs() {}
}
```

**Tests à implémenter :**

```
INSCRIPTION :
  inscrire_DonneesValides_CreeUtilisateurActif
  inscrire_EmailDejaExistant_LeveEmailDejaUtiliseException
  inscrire_EmailInvalide_LeveIllegalArgumentException
  inscrire_NomVide_LeveIllegalArgumentException
  inscrire_MotDePasseTropCourt_LeveIllegalArgumentException

CONNEXION :
  connecter_IdentifiantsCorrects_RetourneUtilisateur
  connecter_MotDePasseIncorrect_LeveConnexionException
  connecter_EmailInexistant_LeveConnexionException
  connecter_CompteDesactive_LeveCompteDesactiveException

GESTION :
  desactiver_UtilisateurExistant_MarqueCommeInactif
  desactiver_UtilisateurInexistant_LeveNotFoundException
  listerActifs_TroisActifsUnInactif_RetourneTroisUtilisateurs
```

**À créer :** `InMemoryUserRepository` comme fausse implémentation pour les tests.

---

### Ex 3.4 — Couverture JaCoCo

**Contexte :** Ajoutez JaCoCo à votre projet et analysez la couverture.

**Configuration à ajouter dans le `pom.xml` :**

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.75</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Questions d'analyse :**

1. Lancez `mvn verify` et ouvrez `target/site/jacoco/index.html`
2. Quelle est votre couverture de lignes ? De branches ?
3. Identifiez 3 zones non couvertes → écrivez les tests manquants
4. Quelles lignes de code ne **devraient pas** être testées (à exclure) ?

---

## Niveau 4 — Projet intégré

> **Objectif :** Développer de bout en bout un composant non trivial en TDD pur, avec organisation complète des tests.
> **Durée estimée :** 3h00 (peut être donné comme exercice du soir)

---

### Ex 4.1 — Gestionnaire de tâches (TodoList)

**Contexte :** Implémentez un gestionnaire de tâches complet en TDD.

**Spécifications complètes :**

```
GESTION DES TÂCHES :
  - Une tâche a : titre (obligatoire), description (optionnelle),
    priorité (HAUTE/MOYENNE/BASSE), statut (A_FAIRE/EN_COURS/TERMINEE)
  - Créer une tâche : statut initial = A_FAIRE
  - Commencer une tâche : A_FAIRE → EN_COURS (erreur si déjà EN_COURS ou TERMINEE)
  - Terminer une tâche : EN_COURS → TERMINEE (erreur si A_FAIRE)
  - Lister par statut : filtrer les tâches par statut
  - Lister par priorité : filtrer par priorité, triées par date de création
  - Statistiques : nombre par statut, taux de complétion
  - Recherche : par mot-clé dans le titre ou la description

CONTRAINTES :
  - Titre : 3 à 100 caractères, non vide, non blanc
  - Priorité par défaut : MOYENNE
  - La date de création est immutable
```

**Plan TDD à suivre rigoureusement :**

```
PHASE 1 — Modèle (Tache)
  1. creerTache_TitreValide_StatutAFaire
  2. creerTache_TitreNull_LeveException
  3. creerTache_TitreTropCourt_LeveException
  4. creerTache_TitreTropLong_LeveException
  5. creerTache_SansPriorite_PrioriteDefautMoyenne
  6. creerTache_DateCreationNonNull

PHASE 2 — Transitions d'état
  7. commencer_TacheAFaire_PasseEnCours
  8. commencer_TacheDejaEnCours_LeveException
  9. commencer_TacheTerminee_LeveException
  10. terminer_TacheEnCours_PasseTerminee
  11. terminer_TacheAFaire_LeveException

PHASE 3 — Service (TodoService)
  12. ajouterTache_TacheValide_Ajoutee
  13. listerParStatut_FiltrageCorrect
  14. listerParPriorite_TriParDateCreation
  15. rechercherParMotCle_TrouveDansTitre
  16. rechercherParMotCle_TrouveDansDescription
  17. rechercherParMotCle_CasseInsensible
  18. rechercherParMotCle_AucunResultat_ListeVide

PHASE 4 — Statistiques
  19. statistiques_ListeVide_TauxZero
  20. statistiques_ToutesTerminees_Taux100pct
  21. statistiques_Mixed_TauxCorrect
```

**Structure du projet :**

```
src/test/java/com/formation/todo/
├── model/
│   └── TacheTest.java          (phases 1 et 2)
├── service/
│   └── TodoServiceTest.java    (phases 3 et 4)
└── repository/
    └── InMemoryTodoRepository.java   (fake pour les tests)
```

**Critères de réussite :**

- [ ] Tous les tests écrits **avant** le code
- [ ] Cycle R-G-R respecté pour chaque test
- [ ] Tests organisés avec `@Nested` et `@DisplayName`
- [ ] Couverture JaCoCo > 85%
- [ ] Aucune logique (`if`/`for`) dans les tests
- [ ] `@ParameterizedTest` utilisé pour les cas de validation

---

## Corrigés partiels

> Les corrigés complets sont fournis par le formateur. Voici des amorces pour les exercices clés.

---

### Corrigé — Ex 1.1 (amorce)

```java
class CalculatriceTest {

    @Test
    void additionner_DeuxEntiers_RetourneLaSomme() {
        // Arrange
        Calculatrice calculatrice = new Calculatrice();

        // Act
        int resultat = calculatrice.additionner(2, 3);

        // Assert
        assertEquals(5, resultat,
            "L'addition de 2 et 3 doit retourner 5");
    }
}
```

```java
// Implémentation minimale (GREEN)
public class Calculatrice {
    public int additionner(int a, int b) {
        return a + b;
    }
}
```

---

### Corrigé — Ex 1.4 (amorce)

```java
@Test
void validerAge_AgeNegatif_LeveIllegalArgumentException() {
    // Arrange
    Validateur validateur = new Validateur();

    // Act + Assert
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> validateur.validerAge(-1)
    );
    assertTrue(ex.getMessage().contains("-1"),
        "Le message d'erreur doit contenir la valeur invalide");
}

@Test
void validerAge_AgeValide_PasException() {
    Validateur validateur = new Validateur();
    assertDoesNotThrow(() -> validateur.validerAge(25));
}
```

---

### Corrigé — Ex 2.2 (amorce)

```java
@ParameterizedTest(name = "TVA sur {0}€ à {1} → {2}€")
@CsvSource({
    "100.0, 0.20, 20.0",
    "200.0, 0.10, 20.0",
    "500.0, 0.055, 27.5",
    "0.0,   0.20, 0.0",
    "1000.0, 0.0, 0.0"
})
void calculerTVA_ParametresVaries_RetourneValeurCorrecte(
        double montant, double taux, double attendu) {
    CalculateurTVA calc = new CalculateurTVA();
    assertEquals(attendu, calc.calculer(montant, taux), 0.001);
}

@ParameterizedTest
@ValueSource(doubles = {-0.01, -1.0, -100.0, -0.001})
void deposer_MontantNonPositif_LeveIllegalArgumentException(double montant) {
    CompteBancaire compte = new CompteBancaire(100.0);
    assertThrows(IllegalArgumentException.class, () -> compte.deposer(montant));
}
```

---

*Exercices Jour 1 — Formation TDD Java · © 2026*
