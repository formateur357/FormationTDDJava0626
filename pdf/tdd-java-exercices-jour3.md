# 🏗️ TDD en Java — Exercices Jour 3
### Code Hérité, FitNesse & Outils CI

---

## 📋 Sommaire

- [Rappel & pré-requis](#rappel--pré-requis)
- [Niveau 1 — Code hérité : diagnostic](#niveau-1--code-hérité--diagnostic)
  - [Ex 1.1 — Identifier les problèmes](#ex-11--identifier-les-problèmes)
  - [Ex 1.2 — Tests de caractérisation](#ex-12--tests-de-caractérisation)
  - [Ex 1.3 — Parameterize Constructor](#ex-13--parameterize-constructor)
  - [Ex 1.4 — Extract and Override](#ex-14--extract-and-override)
- [Niveau 2 — Code hérité : mise sous test](#niveau-2--code-hérité--mise-sous-test)
  - [Ex 2.1 — Sprout Method](#ex-21--sprout-method)
  - [Ex 2.2 — Wrap Method](#ex-22--wrap-method)
  - [Ex 2.3 — Briser les dépendances statiques](#ex-23--briser-les-dépendances-statiques)
  - [Ex 2.4 — Refactoring complet d'un module legacy](#ex-24--refactoring-complet-dun-module-legacy)
- [Niveau 3 — FitNesse](#niveau-3--fitnesse)
  - [Ex 3.1 — Installation et premier test](#ex-31--installation-et-premier-test)
  - [Ex 3.2 — Tables ColumnFixture](#ex-32--tables-columnfixture)
  - [Ex 3.3 — Script Tables et scénarios](#ex-33--script-tables-et-scénarios)
  - [Ex 3.4 — Suite FitNesse et intégration CI](#ex-34--suite-fitnesse-et-intégration-ci)
- [Niveau 4 — Outillage CI complet](#niveau-4--outillage-ci-complet)
  - [Ex 4.1 — Pipeline Jenkins/GitHub Actions](#ex-41--pipeline-jenkinsgithub-actions)
  - [Ex 4.2 — SonarQube et Quality Gate](#ex-42--sonarqube-et-quality-gate)
- [Niveau 5 — Projet final intégré](#niveau-5--projet-final-intégré)
  - [Ex 5.1 — Bibliothèque numérique](#ex-51--bibliothèque-numérique)
- [Corrigés partiels](#corrigés-partiels)

---

## Rappel & pré-requis

Les exercices du Jour 3 s'appuient sur les acquis des jours précédents :

- JUnit 5 avec `@Test`, `@BeforeEach`, `@Nested`, `@ParameterizedTest`
- Mockito avec `@Mock`, `@InjectMocks`, `when()`, `verify()`, `ArgumentCaptor`
- Maven avec dépendances JUnit 5 et Mockito configurées

**Nouvelles dépendances pour le Jour 3 :**

```xml
<!-- JaCoCo — couverture de code -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution><goals><goal>prepare-agent</goal></goals></execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>

<!-- Testcontainers — pour Ex 2.4 (optionnel) -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

---

## Niveau 1 — Code hérité : diagnostic

> **Objectif :** Apprendre à lire, caractériser et préparer le traitement d'un code legacy sans le casser.
> **Durée estimée :** 1h30

---

### Ex 1.1 — Identifier les problèmes

**Contexte :** Analysez le code legacy ci-dessous et cataloguez ses problèmes **avant** d'écrire la moindre ligne de test ou de code.

**Code legacy fourni :**

```java
public class FactureManager {

    private static FactureManager instance;
    private Connection conn;

    private FactureManager() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://prod-server:3306/billing", "admin", "P@ssw0rd!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FactureManager getInstance() {
        if (instance == null) instance = new FactureManager();
        return instance;
    }

    public double calcul(String clientId, String type, int qte) {
        double total = 0;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT prix FROM produits WHERE type='" + type + "'"); // ← ?
            while (rs.next()) {
                total = rs.getDouble("prix") * qte;
                if (clientId.startsWith("VIP")) total = total * 0.85; // ← ?
                if (total > 1000) total = total - 50;                  // ← ?
                Statement st2 = conn.createStatement();
                st2.executeUpdate(
                    "INSERT INTO factures VALUES('" + clientId +
                    "','" + type + "'," + total + ",NOW())");
            }
        } catch (Exception e) {}  // ← ?
        return total;
    }

    public static void reset() { instance = null; } // pour les tests ?
}
```

**Grille d'analyse à compléter :**

| # | Problème identifié | Catégorie | Impact | Technique de correction |
|---|--------------------|-----------|--------|------------------------|
| 1 | Singleton avec état mutable | Couplage | Impossible à tester | Injection de dépendances |
| 2 | Connexion BDD dans le constructeur | ? | ? | ? |
| 3 | Credentials en dur dans le code | ? | ? | ? |
| 4 | Injection SQL (`type` non échappé) | ? | ? | ? |
| 5 | Exception silencieuse (catch vide) | ? | ? | ? |
| 6 | Logique métier mélangée à l'accès BDD | ? | ? | ? |
| 7 | Constantes magiques (0.85, 1000, 50) | ? | ? | ? |
| 8 | Méthode `calcul` fait 4 choses à la fois | ? | ? | ? |

**Question bonus :** Pourquoi la méthode `reset()` suggère-t-elle que quelqu'un a essayé de tester ce code ? Que révèle-t-elle sur l'architecture ?

---

### Ex 1.2 — Tests de caractérisation

**Contexte :** Avant de toucher au code legacy, vous devez capturer son comportement actuel. On vous fournit une version simplifiée (sans BDD réelle) pour l'exercice.

**Code legacy simplifié (testable isolément) :**

```java
public class LegacyCalculateur {

    public String formaterMontant(double montant, String devise) {
        if (devise == null) devise = "";
        String s = String.valueOf((int)(montant * 100) / 100.0);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return devise + s;
    }

    public double appliquerRegles(double base, String typeClient, int anciennete) {
        double result = base;
        if (typeClient != null && typeClient.equals("PREMIUM")) {
            result = result * 0.9;
        }
        if (anciennete > 5) {
            result = result - (result * 0.05);
        }
        if (anciennete > 10) {
            result = result - 20;
        }
        if (result < 0) result = 0;
        return result;
    }

    public int[] decomposerMontant(double montant) {
        int euros = (int) montant;
        int centimes = (int) Math.round((montant - euros) * 100);
        return new int[]{ euros, centimes };
    }
}
```

**Étape 1 — Exploration**

Avant d'écrire les tests, **exécutez mentalement** (ou en debug) la méthode `appliquerRegles` pour ces entrées et notez les résultats :

| base | typeClient | anciennete | Résultat attendu (calculé à la main) |
|------|------------|------------|--------------------------------------|
| 100.0 | "PREMIUM" | 3 | ? |
| 100.0 | "STANDARD" | 6 | ? |
| 100.0 | "PREMIUM" | 11 | ? |
| 100.0 | null | 0 | ? |
| 10.0 | "PREMIUM" | 11 | ? |

**Étape 2 — Écrire les tests de caractérisation**

Pour **chaque** combinaison ci-dessus, écrivez un test qui documente le comportement actuel, même s'il semble incorrect :

```java
class LegacyCalculateurCaracterisationTest {

    private LegacyCalculateur calculateur;

    @BeforeEach
    void setUp() { calculateur = new LegacyCalculateur(); }

    // Tests de formaterMontant (au moins 6 cas)
    @Test
    void formaterMontant_Caracterisation_100_EUR() {
        // Exécuter et noter le VRAI résultat
        String resultat = calculateur.formaterMontant(100.0, "EUR");
        assertEquals("???", resultat); // à remplir après exécution
    }

    // Tests de appliquerRegles (au moins 5 cas)
    // Tests de decomposerMontant (au moins 4 cas)
}
```

**Étape 3 — Documentation**

Rédigez en 5 phrases ce que fait réellement `appliquerRegles`, en incluant les cas qui vous semblent être des bugs potentiels.

---

### Ex 1.3 — Parameterize Constructor

**Contexte :** Appliquez la technique "Parameterize Constructor" pour rendre le `LegacyService` ci-dessous testable.

**Avant (non testable) :**

```java
public class RapportLegacy {

    private final DatabaseAccessor db;
    private final FileWriter fileWriter;
    private final EmailSender emailSender;

    public RapportLegacy() {
        // Dépendances créées en dur → impossible à tester
        this.db = new MySQLDatabaseAccessor("jdbc:mysql://prod:3306/db");
        this.fileWriter = new LocalFileWriter("/var/reports/");
        this.emailSender = new SMTPEmailSender("smtp.prod.com", 587);
    }

    public void genererEtEnvoyer(String mois) {
        List<Ligne> donnees = db.requeter("SELECT * FROM ventes WHERE mois='" + mois + "'");
        String contenu = formaterRapport(donnees);
        String chemin = fileWriter.ecrire("rapport_" + mois + ".txt", contenu);
        emailSender.envoyer("direction@entreprise.com",
            "Rapport " + mois, "Voir pièce jointe : " + chemin);
    }

    private String formaterRapport(List<Ligne> donnees) {
        // ... logique de formatage
        return "Rapport généré";
    }
}
```

**À faire :**

1. Créez les interfaces `DatabaseAccessor`, `FileWriter`, `EmailSender`
2. Ajoutez un **nouveau constructeur** avec injection des dépendances (sans supprimer l'ancien)
3. Écrivez **5 tests** utilisant le nouveau constructeur avec des mocks :

```java
// Tests attendus :
void genererEtEnvoyer_DonneesTrouvees_FormateEtEcrit()
void genererEtEnvoyer_FichierEcrit_EnvoieEmail()
void genererEtEnvoyer_EmailContientNomFichier()
void genererEtEnvoyer_AucuneDonnee_RapportVide()
void genererEtEnvoyer_EchecEcriture_NEnvoiePasEmail()
```

**Règle :** Le constructeur original doit **continuer à fonctionner** (pas de régression).

---

### Ex 1.4 — Extract and Override

**Contexte :** Rendez la classe `ReportService` testable via la technique "Extract and Override" sans modifier l'API publique.

**Code legacy :**

```java
public class WeatherReportService {

    public String genererBulletin(String ville) {
        // Appel API externe — non mockable directement
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.meteo.fr/v2/current?ville=" + ville))
            .build();

        try {
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();
            return traiterReponse(json, ville);
        } catch (Exception e) {
            return "Données météo indisponibles pour " + ville;
        }
    }

    private String traiterReponse(String json, String ville) {
        // Parsing JSON simplifié
        if (json.contains("\"temp\":")) {
            int start = json.indexOf("\"temp\":") + 7;
            int end = json.indexOf(",", start);
            String temp = json.substring(start, end).trim();
            return "Température à " + ville + " : " + temp + "°C";
        }
        return "Format de réponse inattendu";
    }
}
```

**À faire :**

1. **Extrayez** `appelAPI(String ville)` en méthode `protected`
2. Créez `TestableWeatherReportService` (sous-classe) qui surcharge `appelAPI()`
3. Écrivez **4 tests** :

```java
// Tests avec la sous-classe testable
void genererBulletin_ReponseNormale_ExtractTemperature()
void genererBulletin_ReponseAvecTemp25_AfficheCorrct()
void genererBulletin_ReponseVide_RetourneFormatInattendu()
void genererBulletin_ExceptionAPI_RetourneMessageErreur()
```

---

## Niveau 2 — Code hérité : mise sous test

> **Objectif :** Appliquer les techniques avancées de remise sous test sur du code legacy réaliste.
> **Durée estimée :** 2h00

---

### Ex 2.1 — Sprout Method

**Contexte :** Ajoutez une nouvelle fonctionnalité à une grosse méthode legacy **sans la modifier**, en utilisant la technique "Sprout Method".

**Code legacy (ne pas modifier le corps de `processerCommande`) :**

```java
public class LegacyOrderProcessor {

    public String processerCommande(String clientId, String[] articles, double montantTotal) {
        // ⚠️ 80 lignes de code legacy non testable ici
        // Accès BDD, logique métier, formatage, envoi email...
        // ... (simulé par le commentaire)

        // Résultat final
        String numeroCommande = "CMD-" + System.currentTimeMillis();

        // NOUVEAU BESOIN : appliquer les points de fidélité
        // Cette logique doit être ajoutée ici
        // appliquerPointsFidelite(clientId, montantTotal);  ← à sprouter

        return numeroCommande;
    }

    // TODO : Créer cette méthode en TDD pur
    // void appliquerPointsFidelite(String clientId, double montant) { ... }
}
```

**Règles des points de fidélité :**

```
- 1 point par euro dépensé (arrondi à l'entier inférieur)
- Bonus × 2 si le client est de type "GOLD" (clientId commence par "G-")
- Bonus × 3 si le client est de type "PLATINUM" (clientId commence par "P-")
- Minimum garanti : 1 point par commande
- Les points sont sauvegardés via FideliteRepository
```

**À faire :**

1. Créez l'interface `FideliteRepository` avec `ajouterPoints(String clientId, int points)`
2. Développez `appliquerPointsFidelite()` en **TDD pur** (cycle R-G-R)
3. Écrivez **8 tests** pour la nouvelle méthode :

```java
// Tests à implémenter
void appliquerPoints_ClientStandard_1PointParEuro()
void appliquerPoints_ClientGold_2PointsParEuro()
void appliquerPoints_ClientPlatinum_3PointsParEuro()
void appliquerPoints_MontantFaible_MinimumUnPoint()
void appliquerPoints_MontantAvecDecimales_ArrondiInferieur()
void appliquerPoints_ClientStandard50Euros_50Points()
void appliquerPoints_ClientGold50Euros_100Points()
void appliquerPoints_PointsSontSauvegardes()
```

---

### Ex 2.2 — Wrap Method

**Contexte :** Ajoutez de la journalisation à une méthode existante sans la modifier, en utilisant "Wrap Method".

**Méthode existante (ne pas modifier) :**

```java
public class PaiementService {

    private final BanqueAPI banqueAPI;

    public PaiementService(BanqueAPI banqueAPI) {
        this.banqueAPI = banqueAPI;
    }

    // Méthode existante — ne pas modifier
    public ResultatPaiement effectuerPaiement(String carteId, double montant) {
        if (montant <= 0) throw new IllegalArgumentException("Montant invalide");
        return banqueAPI.debiter(carteId, montant);
    }
}
```

**Nouveau besoin :** Chaque paiement doit être journalisé (avant et après) avec : timestamp, carteId masquée (4 derniers chiffres), montant, résultat.

**À faire en Wrap Method :**

1. Créez `PaiementServiceAvecLog` qui **étend** `PaiementService`
2. Surchargez `effectuerPaiement()` pour ajouter la journalisation
3. Définissez l'interface `AuditLogger` avec `logDebut(...)` et `logFin(...)`
4. Écrivez **6 tests** pour `PaiementServiceAvecLog` :

```java
void effectuerPaiement_Debut_LogAvantAppelBanque()
void effectuerPaiement_Succes_LogApresAvecResultat()
void effectuerPaiement_Echec_LogApresAvecErreur()
void effectuerPaiement_CarteMasquee_4DerniersChiffres()
void effectuerPaiement_ExceptionBanque_LogToutDeMeme()
void effectuerPaiement_DelegueALaClasseParente()
// → verify que banqueAPI.debiter() est appelé avec les bons arguments
```

---

### Ex 2.3 — Briser les dépendances statiques

**Contexte :** La classe ci-dessous utilise des appels statiques impossibles à mocker. Rendez-la testable.

**Code legacy avec dépendances statiques :**

```java
public class SessionManager {

    public boolean authentifier(String username, String password) {
        // Appel statique à une classe utilitaire non mockable
        String hash = CryptoUtils.hashSHA256(password);
        User user = DatabasePool.getConnection()
                                .query("SELECT * FROM users WHERE login=?", username)
                                .first(User.class);

        if (user == null || !user.getPasswordHash().equals(hash)) {
            AuditLog.enregistrer("ECHEC_AUTH", username, LocalDateTime.now());
            return false;
        }

        String token = TokenFactory.generer(user.getId(), 3600);
        SessionStore.getInstance().stocker(token, user);
        AuditLog.enregistrer("SUCCES_AUTH", username, LocalDateTime.now());
        return true;
    }
}
```

**À faire :**

1. Identifiez les **5 dépendances statiques** et créez une interface wrapper pour chacune
2. Refactorez `SessionManager` pour accepter ces wrappers par injection
3. Créez des `Fake` ou `Mock` pour chaque wrapper
4. Écrivez **6 tests** :

```java
void authentifier_IdentifiantsCorrects_RetourneTrue()
void authentifier_MotDePasseIncorrect_RetourneFalse()
void authentifier_UtilisateurInexistant_RetourneFalse()
void authentifier_EchecAuth_LogueEvenement()
void authentifier_SuccesAuth_CreeSession()
void authentifier_SuccesAuth_LogueEvenement()
```

---

### Ex 2.4 — Refactoring complet d'un module legacy

**Contexte :** Refactorez complètement le module `FactureManager` de l'Ex 1.1 en suivant le processus complet.

**Processus à respecter (étapes séquentielles) :**

```
ÉTAPE 1 — Tests de caractérisation (avec mock de la BDD)
  Écrire 5 tests qui documentent le comportement actuel

ÉTAPE 2 — Extraction des interfaces
  Créer : QueryExecutor, FactureWriter, RemiseCalculateur

ÉTAPE 3 — Parameterize Constructor
  Ajouter le constructeur avec injection (garder l'ancien)
  → Vérifier que les tests de caractérisation passent encore

ÉTAPE 4 — Nommage et constantes
  Renommer methodes et variables (tests toujours verts)
  Extraire les constantes (0.85 → REMISE_VIP, etc.)

ÉTAPE 5 — Séparation des responsabilités
  Extraire RemiseService, FactureRepository
  → Chaque classe testée indépendamment

ÉTAPE 6 — Nouvelles fonctionnalités en TDD
  Ajouter : remise fidélité (> 2 ans → 5% supplémentaire)
  Écrire les tests AVANT d'implémenter
```

**Livrable attendu :**

```java
// Architecture finale cible
public class FactureService {          // remplace FactureManager
    public FactureService(
        ProduitRepository produitRepo,
        FactureRepository factureRepo,
        RemiseService remiseService
    ) { ... }

    public Facture calculerEtSauvegarder(
        String clientId, String typeProduit, int quantite
    ) { ... }
}

public class RemiseService {
    double calculerRemise(String clientId, double montantBase) { ... }
}
```

**Critères de réussite :**

- [ ] Tests de caractérisation écrits avant tout refactoring
- [ ] Chaque étape du refactoring validée par les tests
- [ ] Tests verts après chaque petit pas
- [ ] Couverture finale > 85%
- [ ] Aucune constante magique dans le code final

---

## Niveau 3 — FitNesse

> **Objectif :** Installer FitNesse, créer des tables de test et les relier à des fixtures Java.
> **Durée estimée :** 1h45

---

### Ex 3.1 — Installation et premier test

**Étape 1 — Installation**

```bash
# Créer le dossier de travail
mkdir tp-fitnesse && cd tp-fitnesse

# Télécharger FitNesse standalone
wget https://github.com/unclebob/fitnesse/releases/download/v20231103/fitnesse-standalone.jar

# Lancer FitNesse sur le port 8080
java -jar fitnesse-standalone.jar -p 8080 -d .

# Vérifier l'accès
# Navigateur → http://localhost:8080
```

**Étape 2 — Créer votre première page de test**

Dans le navigateur FitNesse :

1. Allez sur `http://localhost:8080`
2. Cliquez sur "Edit" sur la page d'accueil
3. Ajoutez un lien : `MaSuite`
4. Sauvegardez et cliquez sur `MaSuite`
5. Créez la page en cliquant "Add child"

**Étape 3 — Votre première table**

Sur la page `MaSuite.PremierTest`, ajoutez :

```wiki
!define TEST_SYSTEM {slim}

!path target/classes
!path target/test-classes
!path target/dependency/*.jar

|import                              |
|com.formation.tdd.fixtures          |

!| ColumnFixture : BonjourFixture    |
| prenom      | salutation?          |
| Alice       | Bonjour Alice !      |
| Bob         | Bonjour Bob !        |
| monde       | Bonjour monde !      |
```

**Étape 4 — Créer la Fixture Java**

```java
package com.formation.tdd.fixtures;

public class BonjourFixture {
    public String prenom;

    public String salutation() {
        return "Bonjour " + prenom + " !";
    }
}
```

**Étape 5 — Exécuter**

1. Compilez le projet Maven : `mvn compile test-compile`
2. Cliquez sur "Test" dans FitNesse
3. Observez les cellules vertes/rouges

**Question :** Que se passe-t-il si vous changez "Bonjour" en "Hello" dans la table mais pas dans la fixture ?

---

### Ex 3.2 — Tables ColumnFixture

**Contexte :** Créez des tables FitNesse pour tester le `FactureService` refactorisé.

**Page FitNesse `MaSuite.TestCalculFacture` :**

```wiki
!define TEST_SYSTEM {slim}
!path target/classes
!path target/test-classes
!path target/dependency/*.jar

|import                           |
|com.formation.tdd.fixtures       |

---- Calcul TVA ----

!| ColumnFixture : CalculTVAFixture |
| montantHT | tauxTVA | tva?  | ttc?   |
| 100.00    | 0.20    | 20.00 | 120.00 |
| 200.00    | 0.10    | 20.00 | 220.00 |
| 500.00    | 0.055   | 27.50 | 527.50 |
| 0.00      | 0.20    | 0.00  | 0.00   |

---- Calcul avec remise ----

!| ColumnFixture : CalculRemiseFixture |
| montantHT | tauxRemise | montantApresRemise? |
| 100.00    | 0.10       | 90.00               |
| 200.00    | 0.20       | 160.00              |
| 500.00    | 0.50       | 250.00              |
| 100.00    | 0.00       | 100.00              |

---- Calcul TTC avec remise ----

!| ColumnFixture : CalculTTCAvecRemiseFixture |
| montantHT | tauxRemise | tauxTVA | ttcFinal? |
| 100.00    | 0.10       | 0.20    | 108.00    |
| 200.00    | 0.20       | 0.10    | 176.00    |
| 1000.00   | 0.15       | 0.20    | 1020.00   |
```

**Fixtures Java à créer :**

```java
// Fixture 1
public class CalculTVAFixture {
    public double montantHT;
    public double tauxTVA;

    public double tva() { /* appel au vrai service */ }
    public double ttc() { /* appel au vrai service */ }
}

// Fixture 2
public class CalculRemiseFixture {
    public double montantHT;
    public double tauxRemise;

    public double montantApresRemise() { /* ... */ }
}

// Fixture 3
public class CalculTTCAvecRemiseFixture {
    public double montantHT;
    public double tauxRemise;
    public double tauxTVA;

    public double ttcFinal() { /* ... */ }
}
```

**À faire :**

1. Créez les 3 fixtures Java
2. Configurez le `pom.xml` pour copier les dépendances dans `target/dependency/`
3. Exécutez les tables et atteignez **100% vert**

---

### Ex 3.3 — Script Tables et scénarios

**Contexte :** Utilisez les Script Tables pour tester un flux complet de gestion de compte.

**Page FitNesse `MaSuite.TestCompteBancaire` :**

```wiki
!define TEST_SYSTEM {slim}
!path target/classes
!path target/test-classes
!path target/dependency/*.jar

|import                           |
|com.formation.tdd.fixtures       |

---- Scénario 1 : Dépôts et retraits successifs ----

|script   | CompteBancaireFixture |
|créer compte avec solde initial | 500.00 |
|check    | solde          | 500.00 |
|deposer  | 200.00         |
|check    | solde          | 700.00 |
|retirer  | 150.00         |
|check    | solde          | 550.00 |

---- Scénario 2 : Retrait impossible ----

|script   | CompteBancaireFixture |
|créer compte avec solde initial | 100.00 |
|check    | peutRetirer    | 200.00 | false |
|check    | peutRetirer    | 100.00 | true  |
|check    | peutRetirer    | 0.01   | true  |

---- Scénario 3 : Historique ----

|script          | CompteBancaireFixture         |
|créer compte avec solde initial | 0.00  |
|deposer         | 100.00                        |
|retirer         | 50.00                         |
|check           | nombreOperations       | 2    |
|check           | typeOperation          | 0 | DEPOT  |
|check           | typeOperation          | 1 | RETRAIT |
```

**Fixture Script à créer :**

```java
public class CompteBancaireFixture {
    private CompteBancaire compte;

    public void créerCompteAvecSoldeInitial(double soldeInitial) {
        compte = new CompteBancaire(soldeInitial);
    }

    public void deposer(double montant) { compte.deposer(montant); }
    public void retirer(double montant) { compte.retirer(montant); }
    public double solde() { return compte.getSolde(); }
    public boolean peutRetirer(double montant) { /* ... */ }
    public int nombreOperations() { /* ... */ }
    public String typeOperation(int index) { /* ... */ }
}
```

**Extensions suggérées :**

Ajoutez une 4e table testant les cas d'erreur :

```wiki
---- Scénario 4 : Gestion des erreurs ----

|script          | CompteBancaireFixture              |
|créer compte avec solde initial | 100.00      |
|ensure          | exception sur retrait de | 200.00 |
|check           | solde                    | 100.00 |
```

---

### Ex 3.4 — Suite FitNesse et intégration CI

**Contexte :** Organisez vos tests FitNesse en suite et intégrez-les dans Maven.

**Étape 1 — Structure de suite**

Créez la hiérarchie suivante dans FitNesse :

```
FormationTDD (Suite)
├── SetUp                  ← configuration commune
├── TestCalculFacture      ← Ex 3.2
├── TestCompteBancaire     ← Ex 3.3
├── TestGestionStock       ← à créer
└── TearDown               ← nettoyage
```

**Page SetUp (configuration partagée) :**

```wiki
!define TEST_SYSTEM {slim}
!path target/classes
!path target/test-classes
!path target/dependency/*.jar

|import                        |
|com.formation.tdd.fixtures    |
```

**Étape 2 — Plugin Maven FitNesse**

```xml
<plugin>
    <groupId>uk.co.javahelp.fitnesse</groupId>
    <artifactId>fitnesse-launcher-maven-plugin</artifactId>
    <version>1.4.2</version>
    <configuration>
        <port>9091</port>
        <suite>FormationTDD</suite>
        <resultsDir>${project.build.directory}/fitnesse-results</resultsDir>
    </configuration>
    <executions>
        <execution>
            <id>fitnesse-tests</id>
            <phase>integration-test</phase>
            <goals>
                <goal>run</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Étape 3 — Nouvelle table à créer**

Créez `TestGestionStock` avec au moins **8 lignes de données** pour tester :
- Ajouter du stock
- Retirer du stock
- Calculer la valeur totale du stock
- Alerter quand le stock passe sous le seuil minimum

**Étape 4 — Exécution CI**

```bash
# Lancer les tests FitNesse dans Maven
mvn clean verify

# Vérifier les résultats
ls target/fitnesse-results/
```

---

## Niveau 4 — Outillage CI complet

> **Objectif :** Configurer un pipeline CI complet avec tests, couverture et qualité de code.
> **Durée estimée :** 1h30

---

### Ex 4.1 — Pipeline Jenkins/GitHub Actions

**Contexte :** Créez un pipeline CI pour votre projet de formation.

**Option A — GitHub Actions**

Créez le fichier `.github/workflows/ci-tdd.yml` :

```yaml
name: CI - Formation TDD Java

on:
  push:
    branches: [ main, develop, 'feature/**' ]
  pull_request:
    branches: [ main ]

env:
  JAVA_VERSION: '17'
  MAVEN_OPTS: '-Xmx1024m'

jobs:
  # Job 1 : Tests unitaires rapides
  tests-unitaires:
    name: Tests Unitaires
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Compilation
        run: mvn compile test-compile --batch-mode

      - name: Tests unitaires + couverture
        run: mvn test --batch-mode

      - name: Publier rapport JUnit
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Résultats Tests Unitaires
          path: target/surefire-reports/*.xml
          reporter: java-junit
          fail-on-error: true

      - name: Upload rapport JaCoCo
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: target/site/jacoco/

  # Job 2 : Analyse qualité (dépend du job 1)
  qualite:
    name: Analyse Qualité
    runs-on: ubuntu-latest
    needs: tests-unitaires
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # nécessaire pour SonarQube

      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      # TODO : Compléter avec l'analyse SonarCloud
      - name: Analyse SonarCloud
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          mvn verify sonar:sonar \
            -Dsonar.projectKey=formation-tdd-java \
            -Dsonar.organization=votre-org \
            -Dsonar.host.url=https://sonarcloud.io

  # Job 3 : Tests d'intégration (optionnel)
  tests-integration:
    name: Tests Intégration
    runs-on: ubuntu-latest
    needs: tests-unitaires
    # TODO : Compléter ce job
    steps:
      - uses: actions/checkout@v4
      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven
      - name: Tests intégration
        run: mvn verify -P integration-tests --batch-mode
```

**Questions à répondre :**

1. Pourquoi le job `qualite` dépend-il de `tests-unitaires` ?
2. Que se passe-t-il si `tests-unitaires` échoue ? `qualite` s'exécute-t-il ?
3. Pourquoi `fetch-depth: 0` est-il nécessaire pour SonarQube ?
4. Comment ajouter une notification Slack en cas d'échec ?

**Option B — Jenkinsfile**

Si Jenkins est disponible en local (via Docker), créez le `Jenkinsfile` équivalent :

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build') {
            steps { sh 'mvn compile test-compile --batch-mode' }
        }

        stage('Tests Unitaires') {
            steps {
                sh 'mvn test --batch-mode'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    // TODO : Ajouter publication JaCoCo
                }
            }
        }

        stage('Qualité') {
            steps {
                // TODO : Ajouter analyse SonarQube
                withSonarQubeEnv('SonarQube-Local') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // TODO : Attendre le résultat du Quality Gate
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        failure {
            // TODO : Configurer la notification d'échec
            echo 'Build échoué ! Envoi notification...'
        }
        success {
            echo 'Build réussi ✅'
        }
    }
}
```

**Exercice :** Complétez les `// TODO` dans le Jenkinsfile.

---

### Ex 4.2 — SonarQube et Quality Gate

**Contexte :** Configurez SonarQube et définissez un Quality Gate adapté à votre projet.

**Étape 1 — Lancer SonarQube avec Docker**

```bash
# Lancer SonarQube Community Edition
docker run -d \
  --name sonarqube-formation \
  -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  sonarqube:10-community

# Attendre que SonarQube soit prêt (~1-2 minutes)
docker logs -f sonarqube-formation | grep "SonarQube is operational"

# Accès : http://localhost:9000
# Login : admin / admin (changer au premier accès)
```

**Étape 2 — Configurer le projet**

```bash
# Générer un token dans SonarQube (Admin → Security → Tokens)
# Copier le token généré

# Analyser le projet
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=VOTRE_TOKEN_ICI \
  -Dsonar.projectKey=formation-tdd \
  -Dsonar.projectName="Formation TDD Java"
```

**Étape 3 — Analyser les résultats**

Après l'analyse, répondez dans votre rapport :

| Métrique | Valeur obtenue | Objectif | Statut |
|----------|----------------|----------|--------|
| Bugs | ? | 0 | ✅ / ❌ |
| Vulnérabilités | ? | 0 | ✅ / ❌ |
| Code Smells | ? | < 10 | ✅ / ❌ |
| Couverture (lignes) | ? | > 80% | ✅ / ❌ |
| Duplication | ? | < 3% | ✅ / ❌ |
| Complexité cyclomatique max | ? | < 10 | ✅ / ❌ |

**Étape 4 — Créer un Quality Gate personnalisé**

Dans SonarQube (Quality Gates → Create) :

```
Nom : Formation TDD - Quality Gate

Conditions sur le NOUVEAU CODE :
  ✅ Coverage < 80%          → FAILED
  ✅ Bugs > 0                → FAILED
  ✅ Vulnerabilities > 0     → FAILED
  ✅ Duplicated Lines > 5%   → FAILED
  ✅ Code Smells > 20        → WARNING
```

**Étape 5 — Corriger les issues**

1. Identifiez les **3 issues les plus importantes** détectées par SonarQube
2. Corrigez-les en **TDD** (écrivez un test qui expose le problème, puis corrigez)
3. Relancez l'analyse et vérifiez que le Quality Gate passe au vert

---

## Niveau 5 — Projet final intégré

> **Objectif :** Combiner toutes les pratiques de la formation sur un projet complet de bout en bout.
> **Durée estimée :** 4h00 (exercice de synthèse)

---

### Ex 5.1 — Bibliothèque numérique

**Contexte :** Vous rejoignez une équipe qui maintient un système de gestion de bibliothèque numérique. La moitié du code est legacy (sans tests), l'autre moitié doit être développée en TDD.

**Architecture existante (legacy — à mettre sous test) :**

```java
// ⚠️ Code legacy — ne pas modifier avant de le couvrir de tests
public class LegacyBibliothequeService {

    private static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection("jdbc:h2:mem:biblio");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean emprunterLivre(String isbn, String userId) {
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM livres WHERE isbn='" + isbn + "'");
            if (!rs.next()) return false;
            if (rs.getBoolean("emprunte")) return false;
            conn.createStatement().executeUpdate(
                "UPDATE livres SET emprunte=true, emprunteur='" +
                userId + "' WHERE isbn='" + isbn + "'");
            conn.createStatement().executeUpdate(
                "INSERT INTO emprunts VALUES('" + isbn + "','" +
                userId + "',NOW(),NULL)");
            return true;
        } catch (Exception e) { return false; }
    }

    // ... 200 lignes supplémentaires
}
```

**Nouvelles fonctionnalités à développer en TDD :**

```
FONCTIONNALITÉS NOUVELLES (à développer en TDD pur) :

1. SYSTÈME DE RÉSERVATION
   - Réserver un livre emprunté (file d'attente)
   - Notifier quand le livre est disponible
   - Annuler une réservation
   - Maximum 3 réservations actives par utilisateur

2. GESTION DES AMENDES
   - Durée d'emprunt max : 21 jours
   - Amende : 0,20€ par jour de retard
   - Plafond : 10€ par livre
   - L'emprunt bloqué si amende > 5€ non payée

3. RECOMMANDATIONS
   - Recommander des livres selon les emprunts passés
   - Basé sur les genres des 5 derniers livres empruntés
   - Exclure les livres déjà lus

4. STATISTIQUES
   - Taux de rotation par genre
   - Top 10 des livres les plus empruntés (30 derniers jours)
   - Utilisateurs les plus actifs
```

**Plan de travail complet :**

```
JOUR 1 DU SPRINT :
  ☐ Tests de caractérisation du code legacy (10 tests min)
  ☐ Parameterize Constructor sur LegacyBibliothequeService
  ☐ Extraction des interfaces (LivreRepository, EmpruntRepository)

JOUR 2 DU SPRINT :
  ☐ ReservationService en TDD (15 tests)
  ☐ AmendService en TDD (12 tests)
  ☐ Fixtures FitNesse pour les règles de réservation

JOUR 3 DU SPRINT :
  ☐ RecommandationService en TDD (8 tests)
  ☐ StatistiquesService en TDD (10 tests)
  ☐ Pipeline CI GitHub Actions complet

JOUR 4 DU SPRINT :
  ☐ Analyse SonarQube + correction des issues
  ☐ Couverture JaCoCo > 85% sur le nouveau code
  ☐ Suite FitNesse complète avec 3 tables minimum
```

**Interfaces à créer :**

```java
public interface LivreRepository {
    Optional<Livre> findByIsbn(String isbn);
    List<Livre> findByGenre(String genre);
    List<Livre> findDisponibles();
    Livre save(Livre livre);
}

public interface EmpruntRepository {
    Emprunt save(Emprunt emprunt);
    Optional<Emprunt> findEmpruntActif(String isbn);
    List<Emprunt> findByUserId(String userId);
    List<Emprunt> findEnRetard(); // date retour dépassée
}

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    List<Reservation> findByIsbn(String isbn); // file d'attente
    List<Reservation> findByUserId(String userId);
    int countActivesParUser(String userId);
}

public interface NotificationService {
    void notifierDisponibilite(String userId, Livre livre);
    void notifierRetardImminant(String userId, Emprunt emprunt, int joursRestants);
    void notifierAmende(String userId, double montantAmende);
}
```

**Tables FitNesse à créer (minimum) :**

```wiki
---- Table 1 : Calcul des amendes ----
!| ColumnFixture : AmendeFixture   |
| joursRetard | amende? | bloque?  |
| 0           | 0.00    | false    |
| 1           | 0.20    | false    |
| 7           | 1.40    | false    |
| 25          | 5.00    | true     |
| 50          | 10.00   | true     |

---- Table 2 : Règles de réservation ----
(À compléter avec au moins 6 lignes de données)

---- Table 3 : Flux complet emprunt-retour ----
(Script table avec au moins 10 étapes)
```

**Critères d'évaluation :**

| Critère | Points | Minimum requis |
|---------|--------|----------------|
| Tests de caractérisation | 10 | 8 tests |
| TDD pur sur nouvelles features | 20 | Cycle R-G-R documenté |
| Mockito utilisé correctement | 15 | @Mock, verify, ArgumentCaptor |
| FitNesse — 3 tables opérationnelles | 15 | 100% vert |
| Pipeline CI fonctionnel | 15 | Build vert |
| SonarQube Quality Gate | 15 | 0 bug, 0 vulnérabilité |
| Couverture JaCoCo | 10 | > 85% nouveau code |
| **Total** | **100** | **70 pour valider** |

---

## Corrigés partiels

---

### Corrigé — Ex 1.1 (grille complète)

| # | Problème | Catégorie | Impact | Correction |
|---|----------|-----------|--------|------------|
| 1 | Singleton mutable | Couplage | Non testable | Injection dépendances |
| 2 | BDD dans constructeur | Architecture | Effets de bord | Parameterize Constructor |
| 3 | Credentials en dur | Sécurité | Fuite secrets | Variables d'environnement |
| 4 | Injection SQL | Sécurité | Vulnérabilité critique | PreparedStatement |
| 5 | catch vide | Robustesse | Bugs silencieux | Relancer ou logger |
| 6 | SQL + métier mélangés | SRP | Non maintenable | Séparation en couches |
| 7 | Constantes magiques | Lisibilité | Non compréhensible | Constantes nommées |
| 8 | Méthode fait tout | SRP | Non testable unitairement | Extraction de méthodes |

---

### Corrigé — Ex 1.2 (résultats `appliquerRegles`)

| base | typeClient | anciennete | Résultat |
|------|------------|------------|----------|
| 100.0 | "PREMIUM" | 3 | 90.0 (remise VIP seule) |
| 100.0 | "STANDARD" | 6 | 95.0 (remise ancienneté seule) |
| 100.0 | "PREMIUM" | 11 | 66.35 (VIP × ancienneté × -20) |
| 100.0 | null | 0 | 100.0 (aucune remise) |
| 10.0 | "PREMIUM" | 11 | 0.0 (résultat négatif plafonné à 0) |

---

### Corrigé — Ex 2.1 (amorce Sprout Method)

```java
// Nouvelle méthode ajoutée en TDD — ne touche pas au code legacy
void appliquerPointsFidelite(String clientId, double montant) {
    int pointsBase = Math.max(1, (int) montant);

    int multiplicateur = 1;
    if (clientId.startsWith("G-")) multiplicateur = 2;
    else if (clientId.startsWith("P-")) multiplicateur = 3;

    int pointsTotal = pointsBase * multiplicateur;
    fideliteRepository.ajouterPoints(clientId, pointsTotal);
}

// Tests correspondants
@Test
void appliquerPoints_ClientStandard_1PointParEuro() {
    appliquerPointsFidelite("STD-001", 50.0);
    verify(fideliteRepository).ajouterPoints("STD-001", 50);
}

@Test
void appliquerPoints_MontantFaible_MinimumUnPoint() {
    appliquerPointsFidelite("STD-001", 0.50);
    verify(fideliteRepository).ajouterPoints("STD-001", 1);
}
```

---

### Corrigé — Ex 3.2 (fixture Java complète)

```java
public class CalculTVAFixture {
    public double montantHT;
    public double tauxTVA;

    private final CalculateurTVA calculateur = new CalculateurTVA();

    public double tva() {
        return Math.round(montantHT * tauxTVA * 100.0) / 100.0;
    }

    public double ttc() {
        return Math.round((montantHT + tva()) * 100.0) / 100.0;
    }
}
```

---

*Exercices Jour 3 — Formation TDD Java · © 2026*
