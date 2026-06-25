# 📖 Mémento TDD Java
### Tableaux récapitulatifs · Cas d'usage · Exemples de référence

---

## 📋 Sommaire

- [1. Le cycle TDD](#1-le-cycle-tdd)
- [2. Patron AAA / Given-When-Then](#2-patron-aaa--given-when-then)
- [3. Annotations JUnit 5](#3-annotations-junit-5)
- [4. Assertions JUnit 5 & AssertJ](#4-assertions-junit-5--assertj)
- [5. Doubles de test](#5-doubles-de-test)
- [6. Mockito — Référence complète](#6-mockito--référence-complète)
- [7. Mockito — Cas d'usage](#7-mockito--cas-dusage)
- [8. Techniques Code Hérité](#8-techniques-code-hérité)
- [9. FitNesse — Référence rapide](#9-fitnesse--référence-rapide)
- [10. Outils CI & Qualité](#10-outils-ci--qualité)
- [11. Bonnes pratiques & Anti-patterns](#11-bonnes-pratiques--anti-patterns)
- [12. Configuration Maven de référence](#12-configuration-maven-de-référence)

---

## 1. Le cycle TDD

### Le cycle Red-Green-Refactor

```
        ┌─────────────────────────────────────────┐
        │                                         │
        ▼                                         │
    🔴 RED                                        │
    Écrire UN test qui échoue                     │
    (erreur compilation ou assertion)             │
        │                                         │
        ▼                                         │
    🟢 GREEN                                      │
    Écrire le MINIMUM de code                     │
    pour faire passer le test                     │
        │                                         │
        ▼                                         │
    🔵 REFACTOR                                   │
    Améliorer sans changer                        │
    le comportement observable ───────────────────┘
```

### Règles fondamentales

| Règle | Description |
|-------|-------------|
| **Un seul test à la fois** | Ne jamais écrire deux tests rouges simultanément |
| **Minimum de code** | En phase GREEN, écrire exactement ce qu'il faut pour passer |
| **Tests verts avant refactor** | Ne refactorer que quand tous les tests passent |
| **Relancer tous les tests** | Après chaque modification, relancer toute la suite |
| **Baby steps** | Décomposer en incréments de 2 à 10 minutes |

### Cadence recommandée

| Phase | Durée typique | Signal de sortie |
|-------|---------------|-----------------|
| RED | 2 à 3 min | Test compile et échoue |
| GREEN | 1 à 5 min | Test passe (barre verte) |
| REFACTOR | 2 à 5 min | Tests toujours verts |

---

## 2. Patron AAA / Given-When-Then

### Structure AAA

```java
@Test
void methode_Contexte_ResultatAttendu() {

    // ARRANGE — Préparer le contexte
    // Créer les objets, configurer les mocks,
    // préparer les données d'entrée

    // ACT — Exécuter l'action testée
    // UN SEUL appel de méthode ici

    // ASSERT — Vérifier le résultat
    // Une ou plusieurs assertions sur le même résultat
}
```

### Correspondance AAA ↔ Given-When-Then

| AAA | Given-When-Then | Rôle |
|-----|-----------------|------|
| Arrange | Given | Contexte initial |
| Act | When | Action déclenchée |
| Assert | Then | Résultat attendu |

### Nommage des tests

```
Convention : methode_Contexte_ResultatAttendu()

✅ Bons exemples :
   calculerTVA_TauxNormal_RetourneValeurCorrecte()
   connexion_MotDePasseIncorrect_RetourneFalse()
   retirer_SoldeInsuffisant_LeveSoldeInsuffisantException()
   deposer_MontantNegatif_LeveIllegalArgumentException()

❌ Mauvais exemples :
   test1()
   testCalculer()
   monTest()
   testOk()
```

---

## 3. Annotations JUnit 5

### Annotations essentielles

| Annotation | Portée | Rôle | Exemple |
|-----------|--------|------|---------|
| `@Test` | Méthode | Marque un test | `@Test void monTest() {}` |
| `@BeforeEach` | Méthode | Avant chaque test | Setup des fixtures |
| `@AfterEach` | Méthode | Après chaque test | Nettoyage |
| `@BeforeAll` | Méthode statique | Une fois avant tous | Démarrer un serveur |
| `@AfterAll` | Méthode statique | Une fois après tous | Arrêter un serveur |
| `@Disabled` | Méthode/Classe | Désactiver | `@Disabled("Bug #123")` |
| `@DisplayName` | Méthode/Classe | Nom lisible | `@DisplayName("...")` |
| `@Nested` | Classe interne | Grouper les tests | Voir ci-dessous |
| `@Tag` | Méthode/Classe | Catégoriser | `@Tag("integration")` |

### Annotations de tests paramétrés

| Annotation | Source de données | Exemple |
|-----------|-------------------|---------|
| `@ValueSource` | Valeurs primitives | `@ValueSource(ints = {1, 2, 3})` |
| `@CsvSource` | Tableau CSV inline | `@CsvSource({"1, 2, 3", "4, 5, 9"})` |
| `@CsvFileSource` | Fichier CSV | `@CsvFileSource(resources = "/data.csv")` |
| `@MethodSource` | Méthode statique | `@MethodSource("fournirDonnees")` |
| `@EnumSource` | Valeurs d'un enum | `@EnumSource(TypeCompte.class)` |
| `@NullSource` | `null` uniquement | `@NullSource` |
| `@EmptySource` | Chaîne/collection vide | `@EmptySource` |
| `@NullAndEmptySource` | `null` + vide | `@NullAndEmptySource` |

### Annotations conditionnelles

| Annotation | Condition |
|-----------|-----------|
| `@EnabledOnOs(OS.LINUX)` | Seulement sous Linux |
| `@DisabledOnOs(OS.WINDOWS)` | Pas sous Windows |
| `@EnabledForJreRange(min = JRE.JAVA_17)` | Java 17+ |
| `@EnabledIfSystemProperty(named="env", matches="CI")` | Variable système |
| `@Timeout(value = 500, unit = MILLISECONDS)` | Timeout strict |

### Exemple @Nested complet

```java
@DisplayName("CompteBancaire")
class CompteBancaireTest {

    private CompteBancaire compte;

    @BeforeEach
    void setUp() { compte = new CompteBancaire(500.0); }

    @Nested
    @DisplayName("Opérations de dépôt")
    class DepotTest {

        @Test
        void deposer_MontantPositif_AugmenteSolde() { /* ... */ }

        @ParameterizedTest
        @ValueSource(doubles = {-1, -100, 0})
        void deposer_MontantNonPositif_LeveException(double montant) { /* ... */ }
    }

    @Nested
    @DisplayName("Opérations de retrait")
    class RetraitTest {
        // ...
    }
}
```

---

## 4. Assertions JUnit 5 & AssertJ

### Assertions JUnit 5 — Tableau de référence

| Assertion | Usage | Exemple |
|-----------|-------|---------|
| `assertEquals(expected, actual)` | Égalité | `assertEquals(5, calc.additionner(2,3))` |
| `assertEquals(exp, act, delta)` | Égalité avec tolérance (double) | `assertEquals(20.0, tva, 0.001)` |
| `assertNotEquals(unexpected, actual)` | Inégalité | `assertNotEquals(0, id)` |
| `assertTrue(condition)` | Vrai | `assertTrue(user.isActif())` |
| `assertFalse(condition)` | Faux | `assertFalse(liste.isEmpty())` |
| `assertNull(object)` | Nul | `assertNull(user.getToken())` |
| `assertNotNull(object)` | Non nul | `assertNotNull(resultat)` |
| `assertThrows(Type.class, lambda)` | Lève exception | Voir ci-dessous |
| `assertDoesNotThrow(lambda)` | Pas d'exception | `assertDoesNotThrow(() -> service.init())` |
| `assertAll(executables...)` | Groupe assertions | Voir ci-dessous |
| `assertSame(expected, actual)` | Même référence | `assertSame(singleton, autre)` |
| `assertIterableEquals(exp, act)` | Collections égales | Même éléments, même ordre |
| `assertArrayEquals(exp, act)` | Tableaux égaux | Éléments identiques |

### assertThrows — Patterns

```java
// Pattern 1 : vérifier le type seulement
assertThrows(IllegalArgumentException.class,
    () -> service.valider(-1));

// Pattern 2 : capturer et inspecter l'exception
IllegalArgumentException ex = assertThrows(
    IllegalArgumentException.class,
    () -> service.valider(-1)
);
assertTrue(ex.getMessage().contains("-1"));

// Pattern 3 : vérifier que PAS d'exception
assertDoesNotThrow(() -> service.valider(42));
```

### assertAll — Grouper les assertions

```java
// Toutes les assertions sont évaluées même si une échoue
assertAll("Profil utilisateur",
    () -> assertEquals("Alice", user.getNom()),
    () -> assertEquals("alice@email.com", user.getEmail()),
    () -> assertTrue(user.isActif()),
    () -> assertNotNull(user.getDateCreation()),
    () -> assertEquals(Role.USER, user.getRole())
);
```

### AssertJ — Assertions fluides (recommandées)

| Assertion AssertJ | Équivalent JUnit 5 |
|-------------------|--------------------|
| `assertThat(val).isEqualTo(5)` | `assertEquals(5, val)` |
| `assertThat(val).isNotNull()` | `assertNotNull(val)` |
| `assertThat(val).isTrue()` | `assertTrue(val)` |
| `assertThat(str).contains("abc")` | `assertTrue(str.contains("abc"))` |
| `assertThat(str).startsWith("A")` | `assertTrue(str.startsWith("A"))` |
| `assertThat(list).hasSize(3)` | `assertEquals(3, list.size())` |
| `assertThat(list).isEmpty()` | `assertTrue(list.isEmpty())` |
| `assertThat(list).contains("A","B")` | `assertTrue(list.containsAll(...))` |
| `assertThat(val).isBetween(1, 10)` | `assertTrue(val >= 1 && val <= 10)` |
| `assertThat(obj).isInstanceOf(User.class)` | `assertTrue(obj instanceof User)` |

```java
// Chaînage AssertJ
assertThat(utilisateur)
    .isNotNull()
    .extracting(User::getNom, User::getEmail)
    .containsExactly("Alice", "alice@email.com");

assertThat(liste)
    .isNotEmpty()
    .hasSize(3)
    .extracting(Produit::getNom)
    .containsExactlyInAnyOrder("Widget", "Gadget", "Outil");
```

---

## 5. Doubles de test

### Les 5 types — Tableau de référence

| Type | Retourne des valeurs | Vérifie les appels | Logique interne | Cas d'usage typique |
|------|---------------------|-------------------|-----------------|---------------------|
| **Dummy** | Non (null/vide) | Non | Non | Paramètre obligatoire non utilisé dans le test |
| **Stub** | Oui (prédéfini) | Non | Non | Contrôler les données retournées par une dépendance |
| **Fake** | Oui | Non | Oui (simplifiée) | Remplacer une BDD ou un service externe par une implémentation en mémoire |
| **Spy** | Oui (enregistre) | Optionnel | Optionnel | Observer ce qui a été appelé sans bibliothèque |
| **Mock** | Oui (configurable) | **Oui** | Non | Vérifier qu'une interaction a bien eu lieu |

### Arbre de décision — Quel double choisir ?

```
Ai-je besoin de vérifier que la dépendance a été appelée ?
    │
    ├── OUI → Mock (Mockito)
    │
    └── NON → Ai-je besoin d'un comportement réaliste complet ?
                  │
                  ├── OUI → Fake (implémentation en mémoire)
                  │
                  └── NON → Ai-je besoin de contrôler les valeurs retournées ?
                                │
                                ├── OUI → Stub (valeurs fixes) ou Mock avec when()
                                │
                                └── NON → Le paramètre est-il requis mais inutile ?
                                              │
                                              ├── OUI → Dummy
                                              └── NON → Spy
```

### Stub vs Mock — Distinction clé

```java
// STUB → Teste l'ÉTAT
@Test
void reserver_PlaceDisponible_ReducitNombrePlaces() {
    // Le stub fournit un contexte
    when(evenementRepo.findById(1L)).thenReturn(Optional.of(evenementAvecPlaces));

    service.reserver(1L, "user1");

    // On vérifie le RÉSULTAT (état)
    assertEquals(9, evenementCapture.getNombrePlaces());
}

// MOCK → Teste le COMPORTEMENT
@Test
void reserver_Succes_EnvoieEmailConfirmation() {
    when(evenementRepo.findById(1L)).thenReturn(Optional.of(evenement));

    service.reserver(1L, "user1");

    // On vérifie l'INTERACTION
    verify(emailService).envoyerConfirmation(eq("user1"), any(Reservation.class));
}
```

---

## 6. Mockito — Référence complète

### Setup

```java
// Option 1 : Extension JUnit 5 (recommandée)
@ExtendWith(MockitoExtension.class)
class MonTest {
    @Mock DependanceA dep;
    @InjectMocks ClasseSousTest service;
}

// Option 2 : Création manuelle
DependanceA dep = mock(DependanceA.class);
ClasseSousTest service = new ClasseSousTest(dep);

// Option 3 : Spy sur instance existante
@Spy
MaClasseReelle spy = new MaClasseReelle();
// ou
MaClasseReelle spy = spy(new MaClasseReelle());
```

### Stubbing — `when().then*()`

| Méthode | Usage | Exemple |
|---------|-------|---------|
| `thenReturn(valeur)` | Retourner une valeur | `when(repo.find(1L)).thenReturn(user)` |
| `thenReturn(v1, v2, v3)` | Valeurs successives | `when(g.next()).thenReturn(1, 2, 3)` |
| `thenThrow(exception)` | Lancer une exception | `when(api.call()).thenThrow(new IOException())` |
| `thenAnswer(inv -> ...)` | Comportement dynamique | `when(r.save(any())).thenAnswer(inv -> inv.getArgument(0))` |
| `thenCallRealMethod()` | Déléguer à l'implémentation | `when(spy.calc()).thenCallRealMethod()` |

`verify(emailService).envoyer(eq("alice@email.com"), anyString());` | assertion comportementale

### Stubbing pour méthodes void — `do*().when()`

| Méthode | Usage |
|---------|-------|
| `doReturn(val).when(spy).methode()` | Stub sur @Spy sans appel réel |
| `doThrow(ex).when(mock).methodeVoid()` | Exception sur méthode void |
| `doNothing().when(mock).methodeVoid()` | Ignorer un appel void |
| `doAnswer(inv -> ...).when(mock).methode()` | Comportement dynamique sur void |

### Matchers d'arguments

| Matcher | Usage |
|---------|-------|
| `any()` | N'importe quel objet (y compris null) |
| `any(Type.class)` | N'importe quel objet du type donné |
| `anyString()` | N'importe quelle String non nulle |
| `anyInt()`, `anyLong()`, `anyDouble()` | N'importe quel primitif |
| `anyList()`, `anyMap()`, `anyCollection()` | N'importe quelle collection |
| `eq(valeur)` | Valeur exacte (utile avec d'autres matchers) |
| `isNull()` | Explicitement null |
| `isNotNull()` | Explicitement non null |
| `argThat(predicate)` | Prédicat personnalisé |
| `startsWith("A")` | String commençant par "A" |

> ⚠️ **Règle d'or :** Si un argument utilise un matcher, TOUS les arguments doivent utiliser des matchers (utiliser `eq()` pour les valeurs exactes).

```java
// ❌ Mélange interdit
when(service.methode("exact", anyInt())).thenReturn(val);

// ✅ Tout en matchers
when(service.methode(eq("exact"), anyInt())).thenReturn(val);
```

### Vérification — `verify()`

| Expression | Signification |
|-----------|---------------|
| `verify(mock).methode(arg)` | Appelée exactement 1 fois |
| `verify(mock, times(3)).methode(arg)` | Appelée exactement 3 fois |
| `verify(mock, never()).methode(arg)` | Jamais appelée |
| `verify(mock, atLeastOnce()).methode(arg)` | Au moins 1 fois |
| `verify(mock, atLeast(2)).methode(arg)` | Au moins 2 fois |
| `verify(mock, atMost(3)).methode(arg)` | Au plus 3 fois |
| `verifyNoMoreInteractions(mock)` | Aucune autre interaction |
| `verifyNoInteractions(mock)` | Aucune interaction du tout |

### InOrder — Vérification d'ordre

```java
// Vérifier l'ordre des appels sur plusieurs mocks
InOrder inOrder = inOrder(serviceA, serviceB, serviceC);
inOrder.verify(serviceA).etape1();
inOrder.verify(serviceB).etape2();
inOrder.verify(serviceC).etape3();
```

### ArgumentCaptor

```java
// Capturer un argument pour l'inspecter
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(repository).save(captor.capture());
User userSauvegarde = captor.getValue();
assertEquals("alice@email.com", userSauvegarde.getEmail());

// Capturer plusieurs appels
verify(emailService, times(3)).envoyer(captor.capture());
List<User> tousLesUsers = captor.getAllValues();
assertEquals(3, tousLesUsers.size());

// Annotation (avec @ExtendWith)
@Captor
ArgumentCaptor<User> userCaptor;
```

---

## 7. Mockito — Cas d'usage

### Cas 1 — Service avec dépendances multiples

```java
@ExtendWith(MockitoExtension.class)
class CommandeServiceTest {

    @Mock CommandeRepository repo;
    @Mock PaiementService paiement;
    @Mock EmailService email;
    @InjectMocks CommandeService service;

    @Test
    void passerCommande_Succes_DebitePuisNotifie() {
        // Arrange
        Commande cmd = new Commande(100.0);
        when(paiement.debiter(anyString(), eq(100.0))).thenReturn("TX-001");

        // Act
        service.passer(cmd, "carte-001", "alice@email.com");

        // Assert — ordre vérifié
        InOrder order = inOrder(paiement, repo, email);
        order.verify(paiement).debiter("carte-001", 100.0);
        order.verify(repo).save(cmd);
        order.verify(email).confirmer("alice@email.com", any());
    }
}
```

### Cas 2 — Tester une exception et ses effets de bord

```java
@Test
void passerCommande_EchecPaiement_NeSauvegardePas() {
    // Arrange
    when(paiement.debiter(anyString(), anyDouble()))
        .thenThrow(new PaiementException("Carte refusée"));

    // Act + Assert
    assertThrows(PaiementException.class,
        () -> service.passer(commande, "carte-001", "alice@email.com"));

    // Vérifier les effets de bord
    verify(repo, never()).save(any());
    verify(email, never()).confirmer(anyString(), any());
}
```

### Cas 3 — ArgumentCaptor pour valider les données sauvegardées

```java
@Test
void inscrire_MotDePasse_EstHasheAvantSauvegarde() {
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

    service.inscrire("Alice", "alice@email.com", "monMotDePasse");

    verify(repo).save(captor.capture());
    User saved = captor.getValue();
    assertNotEquals("monMotDePasse", saved.getMotDePasse()); // hashé
    assertNotNull(saved.getMotDePasse());
}
```

### Cas 4 — Simuler des comportements successifs

```java
@Test
void retry_EchoueDeuxFoisPuisReussit_RetourneResultat() {
    when(apiService.appeler())
        .thenThrow(new IOException("timeout"))  // 1er appel : échec
        .thenThrow(new IOException("timeout"))  // 2e appel : échec
        .thenReturn("Succès");                  // 3e appel : ok

    String resultat = retryService.appelerAvecRetry(3);

    assertEquals("Succès", resultat);
    verify(apiService, times(3)).appeler();
}
```

### Cas 5 — @Spy pour stubbing partiel

```java
@ExtendWith(MockitoExtension.class)
class RapportServiceTest {

    @Spy
    RapportService service; // utilise la vraie implémentation par défaut

    @Test
    void generer_DonneesMockees_CalculCorrect() {
        // Stuber seulement l'accès BDD, laisser la logique réelle
        doReturn(List.of(new Vente(1000), new Vente(2000)))
            .when(service).chargerVentes(anyInt(), anyInt());

        Rapport rapport = service.genererMensuel(1, 2024);

        // La vraie méthode calculerCA() a été appelée
        assertEquals(3000.0, rapport.getCA(), 0.001);
    }
}
```

### Cas 6 — thenAnswer pour comportement dynamique

```java
@Test
void save_NouvelElement_AssigneIdAutoIncrement() {
    AtomicLong idSequence = new AtomicLong(1);

    when(repository.save(any(Produit.class))).thenAnswer(inv -> {
        Produit p = inv.getArgument(0);
        p.setId(idSequence.getAndIncrement());
        return p;
    });

    Produit p1 = service.creer("Widget");
    Produit p2 = service.creer("Gadget");

    assertEquals(1L, p1.getId());
    assertEquals(2L, p2.getId());
}
```

---

## 8. Techniques Code Hérité

### Tableau des techniques

| Technique | Problème résolu | Risque | Quand l'utiliser |
|-----------|-----------------|--------|-----------------|
| **Parameterize Constructor** | `new` dans le code, dépendances dures | Faible | Toujours possible, point de départ recommandé |
| **Extract and Override** | Méthodes non mockables, appels système | Moyen | Code avec méthodes protégées extractibles |
| **Sprout Method** | Ajouter comportement sans toucher l'existant | Très faible | Nouvelle fonctionnalité dans grosse méthode |
| **Sprout Class** | Nouvelle responsabilité importante | Faible | Comportement complexe à ajouter |
| **Wrap Method** | Ajouter avant/après sur méthode existante | Faible | Logging, audit, timing |
| **Wrap Class** | Décorer une classe sans la modifier | Faible | Decorator pattern |
| **Break Static Dep.** | Méthodes statiques non mockables | Moyen | Singletons, utilitaires statiques |

### Tests de caractérisation — Template

```java
// Template pour tests de caractérisation
class LegacyClasseCaracterisationTest {

    @Test
    void caracterisation_scenario_comportementActuel() {
        // IMPORTANT : Ce test documente le comportement ACTUEL,
        // pas nécessairement le comportement CORRECT.
        // Il sera mis à jour si le comportement est intentionnellement modifié.

        LegacyClasse legacy = new LegacyClasse();
        Object resultat = legacy.methode(entree);

        // Assertion sur le résultat réellement observé
        assertEquals(VALEUR_OBSERVEE, resultat);
        // ↑ Remplir après exécution en debug, pas à la main
    }
}
```

### Processus de mise sous test (résumé)

```
ÉTAPE 1 — Ne toucher à rien
  ↓ Lire, comprendre, cartographier les dépendances

ÉTAPE 2 — Tests de caractérisation
  ↓ Documenter le comportement actuel (même incorrect)

ÉTAPE 3 — Identifier la technique applicable
  ↓ Choisir selon le tableau ci-dessus

ÉTAPE 4 — Appliquer la technique (petit pas)
  ↓ Vérifier que les tests de caractérisation passent encore

ÉTAPE 5 — TDD sur les nouvelles fonctionnalités
  ↓ Cycle R-G-R normal

ÉTAPE 6 — Refactoring progressif
  ↓ À chaque cycle, améliorer un peu
```

### Parameterize Constructor — Pattern

```java
// AVANT (non testable)
public class MonService {
    private final DepBDD db = new MySQLDB("jdbc:...");
    public void faire() { db.lire(); }
}

// APRÈS (testable)
public class MonService {
    private final DepBDD db;

    // Nouveau constructeur — pour les tests
    public MonService(DepBDD db) {
        this.db = db;
    }

    // Ancien constructeur — pour la production (compatibilité)
    public MonService() {
        this(new MySQLDB("jdbc:..."));
    }

    public void faire() { db.lire(); }
}
```

---

## 9. FitNesse — Référence rapide

### Types de tables

| Type | Syntaxe | Usage |
|------|---------|-------|
| **ColumnFixture** | `!| ColumnFixture : MaFixture |` | Calculer une valeur pour chaque ligne de données |
| **RowFixture** | `!| RowFixture : MaFixture |` | Vérifier une collection de résultats |
| **Script Table** | `\|script\| MaFixture \|` | Flux séquentiel d'actions |
| **Decision Table** | `\|MaFixture\|` (SLIM) | Table de décision |
| **Query Table** | `\|query: MaFixture\|` | Requête retournant plusieurs lignes |

### Syntaxe SLIM — Référence

```wiki
!define TEST_SYSTEM {slim}            ← Activer SLIM (recommandé)

!path chemin/vers/classes             ← Ajouter au classpath

|import                    |          ← Importer le package
|com.exemple.fixtures      |

!| ColumnFixture : NomFixture  |      ← Déclaration de la table
| entree1 | entree2 | sortie?  |      ← En-têtes (? = méthode)
| val1    | val2    | attend1  |      ← Ligne de données
```

### ColumnFixture — Template Java

```java
public class MaFixture {
    // Champs publics = colonnes d'entrée (sans ?)
    public String entree1;
    public int entree2;

    // Méthodes publiques = colonnes de sortie (avec ? dans la table)
    public double sortie() {
        // Appeler le vrai code à tester
        return new MonService().calculer(entree1, entree2);
    }

    public boolean estValide() {
        return new Validateur().valider(entree1);
    }
}
```

### Script Table — Actions disponibles

| Syntaxe | Action |
|---------|--------|
| `\|methode\|` | Appeler une méthode void |
| `\|methode\| arg \|` | Appeler avec un argument |
| `\|check\| methode \| valeur \|` | Vérifier le retour |
| `\|check not\| methode \| valeur \|` | Vérifier ≠ valeur |
| `\|ensure\| methode \|` | Vérifier que retourne true |
| `\|reject\| methode \|` | Vérifier que retourne false |
| `\|show\| methode \|` | Afficher le retour (pas de vérification) |
| `\|note\| texte \|` | Commentaire |

### Exécution FitNesse en CI

```bash
# Lancer FitNesse en mode CI (sans interface web)
java -jar fitnesse-standalone.jar \
  -c "MaSuite?suite&format=junit" \
  -p 8080

# Via Maven
mvn verify -P fitnesse-tests
```

---

## 10. Outils CI & Qualité

### JaCoCo — Configuration essentielle

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- Instrumenter les classes -->
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <!-- Générer le rapport HTML -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <!-- Vérifier les seuils -->
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
    <configuration>
        <!-- Exclure les classes générées / non pertinentes -->
        <excludes>
            <exclude>**/generated/**</exclude>
            <exclude>**/*DTO.class</exclude>
            <exclude>**/config/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### JaCoCo — Types de couverture

| Indicateur | Ce qu'il mesure | Seuil recommandé |
|-----------|-----------------|-----------------|
| **LINE** | % de lignes exécutées | > 80% |
| **BRANCH** | % de branches if/else couvertes | > 75% |
| **METHOD** | % de méthodes appelées | > 85% |
| **CLASS** | % de classes instanciées | > 90% |
| **INSTRUCTION** | % d'instructions bytecode | > 80% |
| **COMPLEXITY** | Complexité cyclomatique couverte | > 75% |

### Légende rapport JaCoCo

| Couleur | Signification |
|---------|---------------|
| 🟢 Vert | Code couvert par les tests |
| 🔴 Rouge | Code jamais exécuté par les tests |
| 🟡 Jaune | Branche partiellement couverte |

### SonarQube — Métriques principales

| Métrique | Icône | Description | Seuil typique |
|----------|-------|-------------|---------------|
| Bugs | 🐛 | Défauts pouvant causer des erreurs en prod | 0 |
| Vulnerabilities | 🔒 | Failles de sécurité exploitables | 0 |
| Code Smells | 🧹 | Code maintenu mais à améliorer | < 20 |
| Security Hotspots | 🔐 | Code à réviser manuellement | À 0 risque |
| Coverage | 📊 | % de code couvert par les tests | > 80% |
| Duplications | 📋 | % de code dupliqué | < 3% |
| Technical Debt | 💳 | Temps estimé pour corriger tous les smells | < 1 jour |

### Rating SonarQube

| Rating | Bugs / Vulnérabilités | Maintenabilité |
|--------|-----------------------|----------------|
| **A** | 0 | Debt ratio < 5% |
| **B** | 1 mineur | 5-10% |
| **C** | 1 majeur | 10-20% |
| **D** | 1 critique | 20-50% |
| **E** | 1 bloquant | > 50% |

### Comparatif CI — Résumé décisionnel

| Critère | Jenkins | GitHub Actions | GitLab CI |
|---------|---------|----------------|-----------|
| **Hébergement** | Sur site | Cloud GitHub | Sur site / Cloud |
| **Config** | Groovy (Jenkinsfile) | YAML | YAML |
| **Coût infra** | Élevé | Nul | Moyen |
| **Plugins** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Simplicité** | Moyenne | Élevée | Élevée |
| **Idéal pour** | Grandes entreprises | Projets GitHub | Projets GitLab |

---

## 11. Bonnes pratiques & Anti-patterns

### Les règles FIRST

| Lettre | Propriété | Mauvais exemple | Bon exemple |
|--------|-----------|-----------------|-------------|
| **F**ast | Rapide (< 100ms) | Accès BDD réelle | Mock du repository |
| **I**ndependent | Indépendant des autres | Tests ordonnés | `@BeforeEach` isolation |
| **R**epeatable | Même résultat à chaque run | `new Date()` dans les données | Date fixe en fixture |
| **S**elf-Validating | Pass/Fail automatique | `System.out.println` | `assertEquals` |
| **T**imely | Écrit avant ou avec le code | Tests ajoutés 6 mois après | TDD pur |

### Bonnes pratiques — Résumé

| Pratique | ✅ Faire | ❌ Éviter |
|----------|---------|---------|
| **Nommage** | `methode_Contexte_Resultat` | `test1`, `testOk`, `monTest` |
| **Taille du test** | 10 à 20 lignes max | Tests de 50+ lignes |
| **Assertions** | 1 à 3 par test | 10+ assertions dans un test |
| **Logique** | Aucun `if`/`for`/`switch` | Logique conditionnelle dans les tests |
| **Dépendances** | Mocks pour dépendances externes | Vraie BDD, vrai réseau en test unitaire |
| **Fixtures** | `@BeforeEach`, builders | Copier-coller le setup |
| **Messages** | Messages d'erreur explicites | Assertions sans message |
| **Code de test** | Traité comme le code de prod | Négligé, dette accumulée |

### Anti-patterns — Tableau de détection

| Anti-pattern | Symptôme | Conséquence | Correction |
|-------------|----------|-------------|------------|
| **Test qui teste tout** | 20+ assertions | Si 1 échoue, les autres ne s'exécutent pas | Découper en plusieurs tests |
| **Tests couplés** | Ordre d'exécution important | Échecs aléatoires | `@BeforeEach` isolation |
| **Stub sans vérification** | `when()` mais pas `verify()` | Test qui passe toujours | Utiliser `verify()` ou tester l'état |
| **Mock qui en fait trop** | `verify(mock, times(27))` | Fragile au refactoring | Tester le comportement observable |
| **Logique dans le test** | `if (x) { assert... }` | Test non déterministe | `@ParameterizedTest` séparé |
| **Test sans assertion** | Aucun `assertEquals` | Toujours vert, inutile | Ajouter des assertions |
| **Données magiques** | `assertEquals(42, result)` | Incompréhensible | Constantes nommées |
| **Setup trop complexe** | `@BeforeEach` de 30 lignes | Mauvaise conception | Simplifier l'API ou extraire |
| **Accès aux internals** | Réflexion, champs privés | Couplage à l'implémentation | Tester via l'API publique |

---

## 12. Configuration Maven de référence

### pom.xml — Configuration complète pour TDD

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.formation</groupId>
    <artifactId>tdd-java</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Versions des dépendances -->
        <junit.version>5.10.0</junit.version>
        <mockito.version>5.5.0</mockito.version>
        <assertj.version>3.24.2</assertj.version>
        <jacoco.version>0.8.11</jacoco.version>
    </properties>

    <dependencies>
        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Mockito -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- AssertJ (recommandé) -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>${assertj.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- H2 pour les tests d'intégration -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Surefire — exécution des tests JUnit 5 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <!-- Exclure les tests d'intégration des tests unitaires -->
                    <excludes>
                        <exclude>**/*IT.java</exclude>
                        <exclude>**/*IntegrationTest.java</exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!-- Maven Failsafe — tests d'intégration -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <version>3.1.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- JaCoCo — couverture de code -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
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
                            <excludes>
                                <exclude>**/generated/**</exclude>
                                <exclude>**/*DTO.class</exclude>
                            </excludes>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- SonarQube -->
            <plugin>
                <groupId>org.sonarsource.scanner.maven</groupId>
                <artifactId>sonar-maven-plugin</artifactId>
                <version>3.10.0.2594</version>
            </plugin>

            <!-- Copier les dépendances (pour FitNesse) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy-dependencies</id>
                        <phase>package</phase>
                        <goals><goal>copy-dependencies</goal></goals>
                        <configuration>
                            <outputDirectory>${project.build.directory}/dependency</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <!-- Propriétés SonarQube -->
    <properties>
        <sonar.projectKey>formation-tdd-java</sonar.projectKey>
        <sonar.host.url>http://localhost:9000</sonar.host.url>
        <sonar.coverage.jacoco.xmlReportPaths>
            target/site/jacoco/jacoco.xml
        </sonar.coverage.jacoco.xmlReportPaths>
        <sonar.exclusions>
            **/generated/**,
            **/*DTO.java,
            **/config/**
        </sonar.exclusions>
    </properties>

</project>
```

### Commandes Maven essentielles

| Commande | Action |
|----------|--------|
| `mvn test` | Compiler et lancer les tests unitaires |
| `mvn verify` | Tests unitaires + intégration + vérification JaCoCo |
| `mvn test -Dtest=MaClasseTest` | Lancer une seule classe de test |
| `mvn test -Dtest="*Test"` | Lancer toutes les classes *Test |
| `mvn test -Dgroups=unitaire` | Lancer les tests avec @Tag("unitaire") |
| `mvn test -DexcludedGroups=integration` | Exclure les tests @Tag("integration") |
| `mvn sonar:sonar` | Analyser avec SonarQube |
| `mvn jacoco:report` | Générer le rapport JaCoCo |
| `mvn dependency:copy-dependencies` | Copier les JARs (pour FitNesse) |

---

### Imports de référence — Copier-coller prêt à l'emploi

```java
// ===== JUnit 5 =====
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

// ===== Mockito =====
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

// ===== AssertJ =====
import static org.assertj.core.api.Assertions.*;

// ===== Classe de test type =====
@ExtendWith(MockitoExtension.class)
@DisplayName("NomDuService")
class NomDuServiceTest {

    @Mock
    Dependance1 dep1;

    @Mock
    Dependance2 dep2;

    @Captor
    ArgumentCaptor<TypeCapture> captor;

    @InjectMocks
    NomDuService service;

    @BeforeEach
    void setUp() {
        // Setup complémentaire si nécessaire
    }

    @Nested
    @DisplayName("Cas nominaux")
    class CasNominauxTest {

        @Test
        @DisplayName("Description lisible du test")
        void methode_Contexte_ResultatAttendu() {
            // Arrange
            when(dep1.methode(any())).thenReturn(valeur);

            // Act
            ResultatType resultat = service.methode(entree);

            // Assert
            assertThat(resultat).isNotNull();
            verify(dep2).autreMethode(any());
        }
    }
}
```

---

*Mémento TDD Java — Formation 3 jours · © 2026 · Version 1.0*
