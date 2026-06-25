# 🎭 TDD en Java — Exercices Jour 2
### Doubles de Test, Mockito & Techniques d'Écriture

---

## 📋 Sommaire

- [Rappel & pré-requis](#rappel--pré-requis)
- [Niveau 1 — Découverte des doubles](#niveau-1--découverte-des-doubles)
  - [Ex 1.1 — Identifier le bon double](#ex-11--identifier-le-bon-double)
  - [Ex 1.2 — Stub manuel](#ex-12--stub-manuel)
  - [Ex 1.3 — Spy manuel](#ex-13--spy-manuel)
  - [Ex 1.4 — Fake Repository](#ex-14--fake-repository)
- [Niveau 2 — Mockito fondamentaux](#niveau-2--mockito-fondamentaux)
  - [Ex 2.1 — Premier mock Mockito](#ex-21--premier-mock-mockito)
  - [Ex 2.2 — Stubbing conditionnel](#ex-22--stubbing-conditionnel)
  - [Ex 2.3 — Vérification des interactions](#ex-23--vérification-des-interactions)
  - [Ex 2.4 — ArgumentCaptor](#ex-24--argumentcaptor)
- [Niveau 3 — Mockito avancé](#niveau-3--mockito-avancé)
  - [Ex 3.1 — Vérification d'ordre](#ex-31--vérification-dordre)
  - [Ex 3.2 — @Spy partiel](#ex-32--spy-partiel)
  - [Ex 3.3 — Comportements dynamiques](#ex-33--comportements-dynamiques)
  - [Ex 3.4 — Fixtures et Builders](#ex-34--fixtures-et-builders)
- [Niveau 4 — Projet intégré](#niveau-4--projet-intégré)
  - [Ex 4.1 — Système de réservation complet](#ex-41--système-de-réservation-complet)
- [Corrigés partiels](#corrigés-partiels)

---

## Rappel & pré-requis

Avant de commencer, vérifiez que votre projet contient la dépendance Mockito :

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
```

Et l'import statique utile dans vos classes de test :

```java
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
```

---

## Niveau 1 — Découverte des doubles

> **Objectif :** Comprendre les différents types de doubles de test et savoir les implémenter sans bibliothèque.
> **Durée estimée :** 1h30

---

### Ex 1.1 — Identifier le bon double

**Contexte :** Pour chacune des situations ci-dessous, identifiez le type de double le plus adapté (Dummy / Stub / Fake / Spy / Mock) et justifiez votre choix.

**Situation A**

```java
// Vous testez un service de calcul de prix.
// Le service prend un Logger en paramètre mais vous ne voulez
// pas vérifier ce qui est loggué, juste que le calcul fonctionne.
public class PrixService {
    public PrixService(Logger logger) { ... }
    public double calculer(double ht, double taux) { ... }
}
```

→ Type de double pour `Logger` : ____________
→ Justification : ____________

**Situation B**

```java
// Vous testez un service d'envoi d'email.
// Vous voulez vérifier que l'email est bien envoyé
// à la bonne adresse avec le bon sujet.
public interface EmailService {
    void envoyer(String destinataire, String sujet, String corps);
}
```

→ Type de double pour `EmailService` : ____________
→ Justification : ____________

**Situation C**

```java
// Vous testez un service qui lit des données depuis une API externe.
// L'API est lente et instable. Vous voulez contrôler les données retournées.
public interface MeteoAPI {
    MeteoData getMeteo(String ville);
}
```

→ Type de double pour `MeteoAPI` : ____________
→ Justification : ____________

**Situation D**

```java
// Vous testez un service qui utilise une base de données.
// Vous voulez que le repository fonctionne réellement
// (lecture, écriture, recherche) mais sans vraie BDD.
public interface ProduitRepository {
    Produit save(Produit p);
    Optional<Produit> findById(Long id);
    List<Produit> findAll();
}
```

→ Type de double pour `ProduitRepository` : ____________
→ Justification : ____________

---

### Ex 1.2 — Stub manuel

**Contexte :** Implémentez manuellement un Stub pour le service météo, **sans utiliser Mockito**.

**Interface à stuber :**

```java
public interface MeteoService {
    String getMeteo(String ville);       // "Ensoleillé", "Nuageux", etc.
    int getTemperature(String ville);    // en degrés Celsius
    boolean isRisquePluie(String ville); // true si risque de pluie
}
```

**Service à tester :**

```java
public class ConseillerVetements {
    private final MeteoService meteoService;

    public ConseillerVetements(MeteoService meteoService) {
        this.meteoService = meteoService;
    }

    public String conseiller(String ville) {
        int temp = meteoService.getTemperature(ville);
        boolean pluie = meteoService.isRisquePluie(ville);

        if (temp < 10) return "Manteau chaud obligatoire";
        if (temp < 20 && pluie) return "Veste imperméable recommandée";
        if (temp < 20) return "Veste légère conseillée";
        if (pluie) return "Parapluie recommandé";
        return "Tenue estivale";
    }
}
```

**Étape 1 — Créez le Stub**

```java
// À compléter
class StubMeteoServiceFroid implements MeteoService {

    private final String meteo;
    private final int temperature;
    private final boolean risquePluie;

    public stubMeteoService(String meteo, int temperature, boolean risquePluie) {
        this.meteo = meteo;
        this.temperature = temperature;
        this.risquePluie = risquePluie;
    }

    @Override
    public String getMeteo(String ville) { return meteo; }

    @Override
    public int getTemperature(String ville) { return temperature; }

    @Override
    public boolean isRisquePluie(String ville) { return risquePluie; }
}
```

**Étape 2 — Écrivez les tests**

Écrivez **4 tests** couvrant les 4 scénarios du conseiller, chacun avec un Stub différent.

```java
class ConseillerVetementsTest {

    @Test
    void conseiller_GrandFroidSansPluie_RecommandeManeauChaud() { 
        // Arrange
        MeteoService meteoService = new StubMeteoService("Froid", 5, false);
        ConseillerVetements conseiller = new ConseillerVetements(meteoService);
        
        // Act
        string conseil = conseiller.conseiller("Paris");

        // Assert
        assertEquals("Manteau chaud obligatoire", conseil);
    }

    @Test
    void conseiller_FraisAvecPluie_RecommandeImperméable() { 
            // Arrange
        MeteoService meteoService = new StubMeteoService("Pluvieux", 15, true);
        ConseillerVetements conseiller = new ConseillerVetements(meteoService);
        
        // Act
        string conseil = conseiller.conseiller("Lille");

        // Assert
        assertEquals("Veste imperméable recommandée", conseil);
     }

    @Test
    void conseiller_FraisSansPluie_RecommandeVesteLegere() { 
            // Arrange
        MeteoService meteoService = new StubMeteoService("Nuageux", 15, false);
        ConseillerVetements conseiller = new ConseillerVetements(meteoService);
        
        // Act
        string conseil = conseiller.conseiller("Nantes");

        // Assert
        assertEquals("Veste légère conseillée", conseil);
     }

    @Test
    void conseiller_ChaudAvecPluie_RecommandeParapluie() { 
                // Arrange
        MeteoService meteoService = new StubMeteoService("Orageux", 25, true);
        ConseillerVetements conseiller = new ConseillerVetements(meteoService);
        
        // Act
        string conseil = conseiller.conseiller("Marseille");

        // Assert
        assertEquals("Parapluie recommandé", conseil);
     }

    @Test
    void conseiller_ChaudSansPluie_RecommandeTenueEstivale() { 
                // Arrange
        MeteoService meteoService = new StubMeteoService("Ensoleillé", 28, false);
        ConseillerVetements conseiller = new ConseillerVetements(meteoService);
        
        // Act
        string conseil = conseiller.conseiller("Nice");

        // Assert
        assertEquals("Tenue estivale", conseil);
     }
}
```

---

### Ex 1.3 — Spy manuel

**Contexte :** Implémentez un Spy pour capturer les appels à un service de notification.

**Interface à espionner :**

```java
public interface NotificationService {
    void notifierParEmail(String destinataire, String message);
    void notifierParSMS(String telephone, String message);
    void notifierParPush(String userId, String titre, String corps);
}
```

**Étape 1 — Créez le Spy**

```java
class SpyNotificationService implements NotificationService {
    private final List<String> destinatairesEmail = new ArrayList<>();
    private final List<String> messagesEmail = new ArrayList<>();

    private final List<String> numerosSMS = new ArrayList<>();
    private final List<String> messagesSMS = new ArrayList<>();

    private final List<String> userIdsPush = new ArrayList<>();
    private final List<String> titresPush = new ArrayList<>();
    private final List<String> corpsPush = new ArrayList<>();

    // Enregistrez TOUS les appels :
    // - Nombre d'appels à chaque méthode
    // - Arguments de chaque appel (liste de destinataires, messages)

    @Override
    public void notifierParEmail(String destinataire, String message) {
        destinairesEmail.add(destinataire);
        messagesEmail.add(message);
    }

    @Override
    public void notifierParSMS(String telephone, String message) {
        numerosSMS.add(telephone);
        messagesSMS.add(message);
    }
    
    @Override
    public void notifierParPush(String userId, String titre, String corps) {
        userIdsPush.add(userId);
        titresPush.add(titre);
        corpsPush.add(corps);
    }

    // Méthodes d'inspection à créer :
    // - getNombreEmailsEnvoyes()
    public int getNombreEmailsEnvoyes() {
        return destinatairesEmail.size();
    }
    // - getNombreSMSEnvoyes()
    public int getNombreSMSEnvoyes() {
        return numerosSMS.size();
    }

    public int getNombrePushEnvoyes() {
        return userIdsPush.size();
    }
    // - aEteNotifieParEmail(String destinataire)
    public boolean aEteNotifieParEmail(String destinataire) {
        return destinatairesEmail.contains(destinataire);
    }
    // - getDernierMessageEmail()
    public int getDernierMessageEmail() {
        if (messagesEmail.isEmpty()) {
            return null;
        }
        
        return messagesEmail.get(messagesEmail.size() - 1);
    }
    // - getTousLesDestinatairesEmail() → List<String>
    public List<String> getTousLesDestinatairesEmail() {
        return new ArrayList<>(destinatairesEmail);
    }

    public List<String> getTousLesMessagesEmail() {
        return new ArrayList<>(messagesEmail);
    }

    public List<String> getDernierNumerosSMS() {
        if (numerosSMS.isEmpty()) {
            return null;
        }

        return numerosSMS.get(numerosSMS.size() - 1)
    }

    public List<String> getDernierMessageSMS() {
        if (messagesSMS.isEmpty()) {
            return null;
        }

        return messagesSMS.get(messagesSMS.size() - 1)    }
}
```

**Étape 2 — Service à tester**

```java
public class AlerteService {
    private final NotificationService notificationService;

    public void declencherAlerteUrgente(List<String> emailsAdmins,
                                        String telephone,
                                        String message) {
        for (String email : emailsAdmins) {
            notificationService.notifierParEmail(email, "[URGENT] " + message);
        }
        notificationService.notifierParSMS(telephone, message);
    }
}
```

**Étape 3 — Écrivez les tests avec votre Spy**

```java
package com.formation.tdd.doubles;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlerteServiceTest {

    @Test
    void declencherAlerteUrgente_3Admins_Envoie3Emails() {
        // Arrange
        SpyNotificationService spyNotification = new SpyNotificationService();
        AlerteService alerteService = new AlerteService(spyNotification);

        List<String> admins = List.of(
            "admin1@entreprise.com",
            "admin2@entreprise.com",
            "admin3@entreprise.com"
        );

        // Act
        alerteService.declencherAlerteUrgente(
            admins,
            "0600000000",
            "Serveur indisponible"
        );

        // Assert
        assertEquals(3, spyNotification.getNombreEmailsEnvoyes());
        assertTrue(spyNotification.aEteNotifieParEmail("admin1@entreprise.com"));
        assertTrue(spyNotification.aEteNotifieParEmail("admin2@entreprise.com"));
        assertTrue(spyNotification.aEteNotifieParEmail("admin3@entreprise.com"));
    }

    @Test
    void declencherAlerteUrgente_PrefixeUrgent_DansTousLesEmails() {
        // Arrange
        SpyNotificationService spyNotification = new SpyNotificationService();
        AlerteService alerteService = new AlerteService(spyNotification);

        List<String> admins = List.of(
            "admin1@entreprise.com",
            "admin2@entreprise.com"
        );

        // Act
        alerteService.declencherAlerteUrgente(
            admins,
            "0600000000",
            "Base de données inaccessible"
        );

        // Assert
        for (String message : spyNotification.getTousLesMessagesEmail()) {
            assertTrue(
                message.startsWith("[URGENT] "),
                "Tous les emails doivent commencer par [URGENT]"
            );
        }
    }

    @Test
    void declencherAlerteUrgente_ToujoursUnSMS_IndependammentDuNombreAdmins() {
        // Arrange
        SpyNotificationService spyNotification = new SpyNotificationService();
        AlerteService alerteService = new AlerteService(spyNotification);

        List<String> admins = List.of();

        // Act
        alerteService.declencherAlerteUrgente(
            admins,
            "0600000000",
            "Incident critique"
        );

        // Assert
        assertEquals(0, spyNotification.getNombreEmailsEnvoyes());
        assertEquals(1, spyNotification.getNombreSMSEnvoyes());
        assertEquals("0600000000", spyNotification.getDernierNumeroSMS());
        assertEquals("Incident critique", spyNotification.getDernierMessageSMS());
    }
}
```

---

### Ex 1.4 — Fake Repository

**Contexte :** Implémentez un `InMemoryProductRepository` (Fake) qui se comporte comme un vrai repository mais en mémoire.

**Interface :**

```java
public interface ProductRepository {
    Product save(Product product);           // sauvegarde et retourne avec ID généré
    Optional<Product> findById(Long id);
    Optional<Product> findByReference(String ref);
    List<Product> findByCategorie(String categorie);
    List<Product> findAll();
    void deleteById(Long id);
    boolean existsByReference(String ref);
    long count();
}
```

**Contraintes pour le Fake :**

- Les IDs sont **auto-incrémentés** (commence à 1)
- `save()` sur un produit existant (même ID) le **met à jour**
- `save()` sur un produit sans ID (null) en **crée un nouveau**
- `deleteById()` sur un ID inexistant **ne fait rien** (pas d'exception)
- La recherche par référence est **insensible à la casse**
- Le Fake doit être **réinitialisable** (méthode `clear()` pour les tests)

**Squelette :**

```java
public class InMemoryProductRepository implements ProductRepository {
    private final Map<Long, Product> store = new HashMap<>();
    private long nextId = 1L;

    public void clear() {
        store.clear();
        nextId = 1L;
    }

    @Override
    public Product save(Product product) {
        if (product.getId == null) {
            product.setId(nextId);
            nextId++
        } else if (product.getId() >= nextId) {
            nextId = product.getId + 1;1
        }

        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Product> findByReference(String ref) {
        if (ref == null) {
            return Optional.empty();
        }

        return store.values()
            .stream()
            .filter(product -> product.getReference() != null)
            .filter(product -> product.getReference().equalsIgnoreCase(ref))
            .findFirst();
    }

    @Override
    public List<Product> findByCategorie(String categorie) {
        return store.values()
            .stream()
            .filter(product -> product.getCategorie() != null)
            .filter(product -> product.getCategorie().equals(categorie))
            .toList();
        }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsByReference(String ref) {
        return findByReference(ref).isPresent();
    }

    @Override
    public long count() {
        return store.size();
    }
}
```

**Tests du Fake lui-même :**

Écrivez **8 tests** pour valider que votre Fake se comporte correctement. Le Fake est une implémentation — il mérite d'être testé !

```
package com.formation.tdd.doubles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
    }

    @Test
    void save_ProduitSansId_GenereIdAutoIncremente() {
        // Arrange
        Product product = new Product("REF-001", "Clavier", "Informatique", 49.99);

        // Act
        Product saved = repository.save(product);

        // Assert
        assertEquals(1L, saved.getId());
        assertEquals(1, repository.count());
    }

    @Test
    void save_DeuxProduitsSansId_GenereIdsDifferents() {
        // Arrange
        Product clavier = new Product("REF-001", "Clavier", "Informatique", 49.99);
        Product souris = new Product("REF-002", "Souris", "Informatique", 19.99);

        // Act
        Product clavierSauvegarde = repository.save(clavier);
        Product sourisSauvegardee = repository.save(souris);

        // Assert
        assertEquals(1L, clavierSauvegarde.getId());
        assertEquals(2L, sourisSauvegardee.getId());
        assertEquals(2, repository.count());
    }

    @Test
    void save_ProduitExistant_MetAJourLeProduit() {
        // Arrange
        Product product = new Product("REF-001", "Clavier", "Informatique", 49.99);
        Product saved = repository.save(product);

        saved.setNom("Clavier mécanique");
        saved.setPrix(89.99);

        // Act
        repository.save(saved);

        // Assert
        Optional<Product> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Clavier mécanique", found.get().getNom());
        assertEquals(89.99, found.get().getPrix(), 0.001);
        assertEquals(1, repository.count());
    }

    @Test
    void findById_IdExistant_RetourneProduit() {
        // Arrange
        Product product = new Product("REF-001", "Clavier", "Informatique", 49.99);
        Product saved = repository.save(product);

        // Act
        Optional<Product> found = repository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Clavier", found.get().getNom());
    }

    @Test
    void findById_IdInexistant_RetourneOptionalVide() {
        // Act
        Optional<Product> found = repository.findById(999L);

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findByReference_CasseDifferente_RetourneProduit() {
        // Arrange
        Product product = new Product("ABC-123", "Écran", "Informatique", 199.99);
        repository.save(product);

        // Act
        Optional<Product> found = repository.findByReference("abc-123");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Écran", found.get().getNom());
    }

    @Test
    void findByCategorie_CategorieExistante_RetourneProduitsCorrespondants() {
        // Arrange
        repository.save(new Product("REF-001", "Clavier", "Informatique", 49.99));
        repository.save(new Product("REF-002", "Souris", "Informatique", 19.99));
        repository.save(new Product("REF-003", "Chaise", "Mobilier", 99.99));

        // Act
        List<Product> produits = repository.findByCategorie("Informatique");

        // Assert
        assertEquals(2, produits.size());
        assertTrue(produits.stream().allMatch(p -> p.getCategorie().equals("Informatique")));
    }

    @Test
    void deleteById_IdExistant_SupprimeProduit() {
        // Arrange
        Product product = repository.save(
            new Product("REF-001", "Clavier", "Informatique", 49.99)
        );

        // Act
        repository.deleteById(product.getId());

        // Assert
        assertEquals(0, repository.count());
        assertTrue(repository.findById(product.getId()).isEmpty());
    }

    @Test
    void deleteById_IdInexistant_NeFaitRien() {
        // Arrange
        repository.save(new Product("REF-001", "Clavier", "Informatique", 49.99));

        // Act + Assert
        assertDoesNotThrow(() -> repository.deleteById(999L));
        assertEquals(1, repository.count());
    }

    @Test
    void existsByReference_ReferenceExistanteAvecCasseDifferente_RetourneTrue() {
        // Arrange
        repository.save(new Product("REF-001", "Clavier", "Informatique", 49.99));

        // Act
        boolean exists = repository.existsByReference("ref-001");

        // Assert
        assertTrue(exists);
    }

    @Test
    void clear_ApresAppel_VideLeRepositoryEtReinitialiseSequence() {
        // Arrange
        repository.save(new Product("REF-001", "Clavier", "Informatique", 49.99));
        repository.clear();

        // Act
        Product saved = repository.save(new Product("REF-002", "Souris", "Informatique", 19.99));

        // Assert
        assertEquals(1L, saved.getId());
        assertEquals(1, repository.count());
    }
}
```

---

## Niveau 2 — Mockito fondamentaux

> **Objectif :** Maîtriser la création de mocks, le stubbing et les vérifications de base avec Mockito.
> **Durée estimée :** 1h45

---

### Ex 2.1 — Premier mock Mockito

**Contexte :** Réécrivez les tests de l'Ex 1.2 et 1.3 **en utilisant Mockito** au lieu des stubs/spy manuels.

**Exercice A — Réécriture du ConseillerVetements**

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConseillerVetementsTest {

    @Mock
    MeteoService meteoService;

    @InjectMocks
    ConseillerVetements conseiller;

    @Test
    void conseiller_GrandFroid_RecommandeManteauChaud() {
        // Arrange
        when(meteoService.getTemperature(anyString())).thenReturn(5);
        when(meteoService.isRisquePluie(anyString())).thenReturn(false);

        // Act
        String conseil = conseiller.conseiller("Paris");

        // Assert
        assertEquals("Manteau chaud obligatoire", conseil);
    }

    @Test
    void conseiller_FraisAvecPluie_RecommandeImpermeable() {
        when(meteoService.getTemperature(anyString())).thenReturn(15);
        when(meteoService.isRisquePluie(anyString())).thenReturn(true);

        String conseil = conseiller.conseiller("Lyon");

        assertEquals("Veste imperméable recommandée", conseil);
    }

    @Test
    void conseiller_FraisSansPluie_RecommandeVesteLegere() {
        when(meteoService.getTemperature(anyString())).thenReturn(15);
        when(meteoService.isRisquePluie(anyString())).thenReturn(false);

        String conseil = conseiller.conseiller("Nantes");

        assertEquals("Veste légère conseillée", conseil);
    }

    @Test
    void conseiller_ChaudAvecPluie_RecommandeParapluie() {
        when(meteoService.getTemperature(anyString())).thenReturn(25);
        when(meteoService.isRisquePluie(anyString())).thenReturn(true);

        String conseil = conseiller.conseiller("Bordeaux");

        assertEquals("Parapluie recommandé", conseil);
    }

    @Test
    void conseiller_ChaudSansPluie_RecommandeTenueEstivale() {
        when(meteoService.getTemperature(anyString())).thenReturn(28);
        when(meteoService.isRisquePluie(anyString())).thenReturn(false);

        String conseil = conseiller.conseiller("Marseille");

        assertEquals("Tenue estivale", conseil);
    }
}
```

**Exercice B — Réécriture du AlerteService**

Réécrivez les 3 tests de l'Ex 1.3 avec un `@Mock NotificationService`.

**Question de réflexion :** Dans l'exercice B, utilisez-vous le mock comme un Stub ou comme un Mock (au sens de la théorie) ? Pourquoi ?

---

### Ex 2.2 — Stubbing conditionnel

**Contexte :** Le service ci-dessous appelle une API de paiement. Configurez des stubs conditionnels.

```java
public interface PaiementAPI {
    PaiementResult payer(String carteId, double montant);
    boolean rembourser(String transactionId);
    StatutCarte getStatutCarte(String carteId);
}

public enum StatutCarte { VALIDE, EXPIREE, BLOQUEE }

public class PaiementService {
    private final PaiementAPI api;

    public Commande payer(Commande commande, String carteId) {
        StatutCarte statut = api.getStatutCarte(carteId);
        if (statut == StatutCarte.EXPIREE)
            throw new CarteExpireException(carteId);
        if (statut == StatutCarte.BLOQUEE)
            throw new CarteBloqueException(carteId);

        PaiementResult result = api.payer(carteId, commande.getMontant());
        if (!result.isSuccess())
            throw new PaiementRefuseException(result.getRaison());

        commande.setStatut(StatutCommande.PAYEE);
        commande.setTransactionId(result.getTransactionId());
        return commande;
    }
}
```

**Écrivez ces tests avec stubbing conditionnel :**

```java
// 1. Carte valide, paiement accepté → commande PAYEE avec transactionId
void payer_CarteValide_CommandePayee()

// 2. Carte expirée → lève CarteExpireException (API payer() jamais appelée)
void payer_CarteExpiree_LeveException_SansAppelPaiement()

// 3. Carte bloquée → lève CarteBloqueException
void payer_CarteBloqueee_LeveException()

// 4. Carte valide mais paiement refusé → lève PaiementRefuseException
void payer_CarteValide_PaiementRefuse_LeveException()

// 5. Carte valide, montants différents → stub répond différemment selon le montant
void payer_GrandMontant_UtilisePaiementSpecial()
```

**Bonus :** Pour le test 2, vérifiez que `api.payer()` n'est **jamais** appelé quand la carte est expirée.

---

### Ex 2.3 — Vérification des interactions

**Contexte :** Vérifiez les interactions avec les mocks dans les scénarios suivants.

**Scénario — CommandeService**

```java
public class CommandeService {
    private final StockService stockService;
    private final PaiementService paiementService;
    private final EmailService emailService;
    private final CommandeRepository repository;

    public Commande passerCommande(Panier panier, String carteId) {
        // 1. Vérifier le stock pour chaque article
        // 2. Réserver le stock
        // 3. Payer
        // 4. Sauvegarder la commande
        // 5. Envoyer email de confirmation
        return commande;
    }
}
```

**Tests à écrire :**

```java
// Test 1 : Toutes les étapes exécutées dans le bon ordre
void passerCommande_Valide_ExecuteToutes5Etapes()
// → Vérifiez que chaque service est appelé EXACTEMENT une fois

// Test 2 : Stock insuffisant → aucun paiement, aucun email
void passerCommande_StockInsuffisant_NiPaiementNiEmail()
// → Utiliser verify(service, never()).methode()

// Test 3 : Paiement échoué → stock libéré, aucun email
void passerCommande_PaiementEchoue_LibereLe_stock_SansEmail()

// Test 4 : Deux articles dans le panier → stockService appelé 2 fois
void passerCommande_DeuxArticles_VerifieStockDeuxFois()
// → Utiliser verify(stockService, times(2)).verifierDisponibilite(any())
```

**Pour chaque test, identifiez :**

- Les appels à **vérifier** (verify)
- Les appels à vérifier comme **jamais exécutés** (never)
- L'**ordre** à vérifier si pertinent (InOrder)

---

### Ex 2.4 — ArgumentCaptor

**Contexte :** Utilisez ArgumentCaptor pour inspecter les arguments passés aux mocks.

**Service :**

```java
public class InscriptionService {
    private final UserRepository repository;
    private final EmailService emailService;
    private final AuditService auditService;

    public User inscrire(InscriptionRequest request) {
        User user = new User(
            request.getNom(),
            request.getEmail().toLowerCase().trim(),
            genererMotDePasseHash(request.getMotDePasse()),
            Role.USER,
            LocalDateTime.now()
        );

        User userSauvegarde = repository.save(user);

        emailService.envoyer(
            user.getEmail(),
            "Bienvenue " + user.getNom() + " !",
            "Votre compte a été créé avec succès."
        );

        auditService.enregistrer(
            "INSCRIPTION",
            user.getId(),
            Map.of("email", user.getEmail(), "ip", request.getIpOrigine())
        );

        return userSauvegarde;
    }
}
```

**Tests avec ArgumentCaptor :**

```java
// Test 1 : L'utilisateur sauvegardé a l'email en minuscules
void inscrire_EmailAvecMajuscules_SauvegardeEnMinuscules()
// → Capturer le User passé à repository.save() et vérifier user.getEmail()

// Test 2 : L'email envoyé contient le prénom de l'utilisateur
void inscrire_NomAlice_EmailContientAlice()
// → Capturer les 3 arguments de emailService.envoyer()

// Test 3 : L'audit contient l'IP d'origine
void inscrire_DepuisIPConnue_AuditContientIP()
// → Capturer le Map passé à auditService.enregistrer()

// Test 4 : Le mot de passe stocké est hashé (pas en clair)
void inscrire_MotDePasse_NestPasStockeEnClair()
// → Capturer le User et vérifier que user.getMotDePasse() != request.getMotDePasse()
```

---

## Niveau 3 — Mockito avancé

> **Objectif :** Maîtriser les fonctionnalités avancées de Mockito : InOrder, Spy, comportements dynamiques, organisation des fixtures.
> **Durée estimée :** 2h00

---

### Ex 3.1 — Vérification d'ordre

**Contexte :** L'ordre des appels est critique pour un processus de validation de commande.

**Processus à tester :**

```
PROCESSUS DE VALIDATION (ordre OBLIGATOIRE) :
  1. Authentifier l'utilisateur
  2. Valider la commande (métier)
  3. Vérifier le stock
  4. Calculer le prix final (avec remises)
  5. Débiter le paiement
  6. Mettre à jour le stock
  7. Générer la facture
  8. Envoyer la confirmation
```

**Interfaces :**

```java
public interface AuthService       { User authentifier(String token); }
public interface ValidationService { void valider(Commande c); }
public interface StockService      { void verifier(Commande c); void mettreAJour(Commande c); }
public interface TarifService      { double calculerPrix(Commande c); }
public interface PaiementService   { String debiter(String carteId, double montant); }
public interface FactureService    { Facture generer(Commande c); }
public interface NotifService      { void confirmer(String email, Facture f); }
```

**Tests à écrire :**

```java
// Test 1 : Ordre complet respecté
void valider_ProcessusComplet_EtapesEnOrdre()
// → InOrder avec tous les services, vérifier la séquence exacte

// Test 2 : Échec authentification → rien d'autre n'est exécuté
void valider_AuthEchouee_AucuneAutreEtape()
// → verify(validationService, never()).valider(any()) etc.

// Test 3 : Échec paiement → stock non mis à jour, pas de facture, pas de notif
void valider_PaiementEchoue_StopApresEtape5()

// Test 4 : Vérification que le prix calculé est bien celui débité
void valider_PrixCalcule_EstBienDebiteSurCarte()
// → Utiliser ArgumentCaptor pour capturer le montant débité
// et vérifier qu'il correspond à ce que tarifService.calculerPrix() a retourné
```

---

### Ex 3.2 — @Spy partiel

**Contexte :** Utilisez `@Spy` pour tester partiellement une classe en stubant seulement certaines méthodes.

**Classe à espionner :**

```java
public class RapportService {
    private final DataSource dataSource;

    // Méthode publique à tester
    public Rapport genererRapportMensuel(int mois, int annee) {
        List<Vente> ventes = chargerVentes(mois, annee);   // appel BDD
        double chiffreAffaires = calculerCA(ventes);        // logique pure
        double marge = calculerMarge(ventes);               // logique pure
        String resume = genererResume(chiffreAffaires, marge); // logique pure
        return new Rapport(mois, annee, chiffreAffaires, marge, resume);
    }

    // Méthode protégée → stubbable via @Spy
    protected List<Vente> chargerVentes(int mois, int annee) {
        // Accès BDD → impossible à tester directement
        return dataSource.query("SELECT ...");
    }

    // Méthodes de logique pure → testables sans mock
    double calculerCA(List<Vente> ventes) { ... }
    double calculerMarge(List<Vente> ventes) { ... }
    String genererResume(double ca, double marge) { ... }
}
```

**Tests à écrire :**

```java
@ExtendWith(MockitoExtension.class)
class RapportServiceTest {

    @Spy
    RapportService rapportService;

    @Test
    void genererRapport_VentesChargees_CACorrect() {
        // Stubber uniquement chargerVentes() pour éviter l'accès BDD
        List<Vente> ventesSimulees = List.of(
            new Vente(1000.0, 700.0),
            new Vente(2000.0, 1200.0)
        );
        doReturn(ventesSimulees).when(rapportService).chargerVentes(1, 2024);

        Rapport rapport = rapportService.genererRapportMensuel(1, 2024);

        assertEquals(3000.0, rapport.getChiffreAffaires(), 0.001);
        // L'implémentation RÉELLE de calculerCA() est appelée → pas de stub
    }

    // Test 2 : Vérifier que le resume est bien généré
    // Test 3 : Vérifier que chargerVentes() est appelé avec les bons paramètres
    // Test 4 : Liste de ventes vide → rapport avec CA = 0, marge = 0
}
```

---

### Ex 3.3 — Comportements dynamiques

**Contexte :** Utilisez `thenAnswer` pour simuler des comportements complexes.

**Scénario A — Simuler une BDD auto-incrémentée**

```java
// Le repository doit assigner un ID auto-généré à la sauvegarde
when(repository.save(any(User.class))).thenAnswer(invocation -> {
    User user = invocation.getArgument(0);
    // TODO : retourner un nouvel objet User avec un ID généré
    // L'ID doit s'incrémenter à chaque appel (1, 2, 3, ...)
});
```

**Scénario B — Simuler une latence réseau**

```java
// API qui répond lentement (pour tester les timeouts)
when(apiExterne.appeler(anyString())).thenAnswer(invocation -> {
    // TODO : simuler un délai de 100ms
    return new ApiResponse("OK");
});
```

**Scénario C — Comportement conditionnel sur l'argument**

```java
// Service de traduction qui répond différemment selon la langue
when(traductionService.traduire(anyString(), anyString()))
    .thenAnswer(invocation -> {
        String texte = invocation.getArgument(0);
        String langue = invocation.getArgument(1);
        // TODO : retourner le texte avec le préfixe "[FR]" ou "[EN]"
        // selon la langue passée en argument
    });
```

**Tests à écrire pour chaque scénario :**

```java
// Scénario A
void inscrire_DeuxUtilisateurs_OntsDesIDsDifferents()
void inscrire_TroisFois_IDsSequentiels()

// Scénario B
void appelerAPI_AvecTimeout100ms_EchoueTimeout50ms()

// Scénario C
void traduire_EnFrancais_AjouttePrefixeFR()
void traduire_EnAnglais_AjouttePrefixeEN()
```

---

### Ex 3.4 — Fixtures et Builders

**Contexte :** Créez des fixtures réutilisables pour les tests de l'ensemble du jour 2.

**Étape 1 — Créez un Builder pour User**

```java
public class UserBuilder {
    // Valeurs par défaut sensibles
    private Long id = null;
    private String nom = "Utilisateur Test";
    private String email = "test@exemple.com";
    private String motDePasseHash = "hash$sécurisé";
    private Role role = Role.USER;
    private boolean actif = true;
    private LocalDateTime dateCreation = LocalDateTime.now();

    // Méthodes de configuration chaînées
    public UserBuilder avecId(Long id) { ... }
    public UserBuilder avecNom(String nom) { ... }
    public UserBuilder avecEmail(String email) { ... }
    public UserBuilder commAdmin() { ... }
    public UserBuilder inactif() { ... }
    public UserBuilder creeLe(LocalDateTime date) { ... }

    public User build() { ... }

    // Méthode factory statique
    public static UserBuilder unUtilisateur() { return new UserBuilder(); }
}
```

**Étape 2 — Créez un Object Mother pour les scénarios de test**

```java
public class TestFixtures {
    // Utilisateurs pré-configurés
    public static User utilisateurActif() { ... }
    public static User administrateur() { ... }
    public static User utilisateurInactif() { ... }
    public static User utilisateurSansId() { ... } // avant persistance

    // Commandes pré-configurées
    public static Commande commandeSimple() { ... }
    public static Commande commandeAvecRemise() { ... }
    public static Commande commandeDejaPayee() { ... }

    // Requests pré-configurées
    public static InscriptionRequest requestInscriptionValide() { ... }
    public static InscriptionRequest requestAvecEmailInvalide() { ... }
}
```

**Étape 3 — Refactorez vos tests précédents**

Reprenez les tests des Ex 2.1 à 2.4 et remplacez les créations d'objets inline par les Builders et Object Mother.

**Avant :**
```java
User user = new User(null, "Alice", "alice@email.com", Role.USER, true, LocalDateTime.now());
```

**Après :**
```java
User user = unUtilisateur().avecNom("Alice").avecEmail("alice@email.com").build();
// ou
User user = TestFixtures.utilisateurActif();
```

---

## Niveau 4 — Projet intégré

> **Objectif :** Implémenter un système complet avec des doubles de test appropriés, Mockito avancé et fixtures de qualité.
> **Durée estimée :** 3h00

---

### Ex 4.1 — Système de réservation complet

**Contexte :** Implémentez en TDD un système de réservation de billets d'événements.

**Domaine métier :**

```
RÈGLES DE RÉSERVATION :
  - Un utilisateur peut réserver 1 à 5 billets par événement
  - Le paiement est débité immédiatement à la réservation
  - Un email de confirmation est envoyé après paiement réussi
  - En cas d'échec paiement : aucun billet réservé, aucun email
  - L'annulation est possible jusqu'à 48h avant l'événement
  - L'annulation entraîne un remboursement et un email
  - Une annulation tardive (< 48h) : remboursement à 50%
  - Chaque réservation a un code unique de 8 caractères alphanumériques
```

**Interfaces :**

```java
public interface EvenementRepository {
    Optional<Evenement> findById(Long id);
    void reserverPlaces(Long evenementId, int nbPlaces);
    void libererPlaces(Long evenementId, int nbPlaces);
}

public interface PaiementGateway {
    PaiementResult debiter(String carteId, double montant, String description);
    RemboursementResult rembourser(String transactionId, double montant);
}

public interface NotificationService {
    void envoyerConfirmation(String email, Reservation reservation);
    void envoyerAnnulation(String email, Reservation reservation, double montantRembourse);
}

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findByCode(String code);
    Optional<Reservation> findById(Long id);
}

public interface CodeGenerateur {
    String generer(); // génère un code unique 8 caractères
}
```

**Plan TDD complet :**

```
PHASE 1 — Réservation nominale
  reserver_EvenementDisponible_RetourneReservationAvecCode
  reserver_EvenementDisponible_ReduitsLesPlacesDisponibles
  reserver_EvenementDisponible_DebiteLePaiement
  reserver_EvenementDisponible_EnvoieEmail
  reserver_CodeUnique_EstGenere

PHASE 2 — Réservation — cas d'erreur
  reserver_EvenementInexistant_LeveEvenementNotFoundException
  reserver_PlusDe5Billets_LeveNombreBilletsException
  reserver_ZeroBillet_LeveNombreBilletsException
  reserver_PasAssezDePlaces_LevePlacesInsuffisantesException
  reserver_PaiementEchoue_AucuneBilletReserve
  reserver_PaiementEchoue_AucunEmailEnvoye

PHASE 3 — Annulation
  annuler_Plus48h_RemboursementTotal
  annuler_Moins48h_Remboursement50pct
  annuler_Exactement48h_RemboursementTotal
  annuler_ReservationInexistante_LeveNotFoundException
  annuler_EnvoieEmailAnnulation
  annuler_LibereLesPlaces

PHASE 4 — Intégrité
  reserver_Succes_CodeEstUnique
  reserver_LesArgumentsCorrectsSontPassesAuPaiement
  annuler_LeBonMontantEstRembourse
```

**Contraintes techniques :**

- Utilisez `@ExtendWith(MockitoExtension.class)` et `@Mock` pour toutes les dépendances
- Utilisez `ArgumentCaptor` pour vérifier les arguments des appels critiques
- Utilisez `InOrder` pour les phases où l'ordre est important
- Créez des fixtures avec Builder et Object Mother
- Organisez avec `@Nested` par phase
- Visez **100% de couverture** sur la classe `ReservationService`

**Classe à implémenter :**

```java
public class ReservationService {
    public ReservationService(
        EvenementRepository evenementRepository,
        ReservationRepository reservationRepository,
        PaiementGateway paiementGateway,
        NotificationService notificationService,
        CodeGenerateur codeGenerateur
    ) { ... }

    public Reservation reserver(Long evenementId, String carteId,
                                 int nbBillets, String emailClient) { ... }

    public void annuler(Long reservationId) { ... }
}
```

---

## Corrigés partiels

---

### Corrigé — Ex 1.1 (réponses)

| Situation | Type de double | Justification |
|-----------|----------------|---------------|
| Logger | **Dummy** | Requis par le constructeur mais non utilisé dans ce test |
| EmailService | **Mock** | On veut vérifier *que* l'email est envoyé à la bonne adresse |
| MeteoAPI | **Stub** | On contrôle les données retournées sans vérifier les appels |
| ProduitRepository | **Fake** | On veut une implémentation fonctionnelle complète mais légère |

---

### Corrigé — Ex 2.1 (amorce)

```java
@ExtendWith(MockitoExtension.class)
class ConseillerVetementsTest {

    @Mock
    MeteoService meteoService;

    @InjectMocks
    ConseillerVetements conseiller;

    @Test
    void conseiller_TempMoinsde10_RecommandeManteauChaud() {
        // Arrange
        when(meteoService.getTemperature("Paris")).thenReturn(5);
        when(meteoService.isRisquePluie("Paris")).thenReturn(false);

        // Act
        String conseil = conseiller.conseiller("Paris");

        // Assert
        assertEquals("Manteau chaud obligatoire", conseil);
        // Pas de verify() car on teste l'ÉTAT (retour de la méthode)
        // et non le COMPORTEMENT (interactions avec le mock)
    }
}
```

---

### Corrigé — Ex 2.4 (amorce)

```java
@Test
void inscrire_EmailAvecMajuscules_SauvegardeEnMinuscules() {
    // Arrange
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(repository.save(any(User.class)))
        .thenAnswer(inv -> inv.getArgument(0)); // retourne l'objet tel quel

    InscriptionRequest request = new InscriptionRequest(
        "Alice", "ALICE@EMAIL.COM", "motdepasse123"
    );

    // Act
    service.inscrire(request);

    // Assert
    verify(repository).save(userCaptor.capture());
    User userSauvegarde = userCaptor.getValue();
    assertEquals("alice@email.com", userSauvegarde.getEmail(),
        "L'email doit être converti en minuscules avant la sauvegarde");
}
```

---

### Corrigé — Ex 3.1 (amorce InOrder)

```java
@Test
void valider_ProcessusComplet_EtapesEnOrdre() {
    // Arrange
    when(authService.authentifier(anyString())).thenReturn(utilisateurValide);
    when(tarifService.calculerPrix(any())).thenReturn(150.0);
    when(paiementService.debiter(anyString(), anyDouble()))
        .thenReturn("TX-001");
    when(factureService.generer(any())).thenReturn(new Facture());

    // Act
    processusService.valider(commande, "token-valid", "carte-001");

    // Assert — vérification de l'ordre
    InOrder ordre = inOrder(
        authService, validationService, stockService,
        tarifService, paiementService, factureService, notifService
    );
    ordre.verify(authService).authentifier("token-valid");
    ordre.verify(validationService).valider(commande);
    ordre.verify(stockService).verifier(commande);
    ordre.verify(tarifService).calculerPrix(commande);
    ordre.verify(paiementService).debiter("carte-001", 150.0);
    ordre.verify(stockService).mettreAJour(commande);
    ordre.verify(factureService).generer(commande);
    ordre.verify(notifService).confirmer(anyString(), any(Facture.class));
}
```

---

*Exercices Jour 2 — Formation TDD Java · © 2026*
