# 📅 TimeTable 2.0

<p align="center">
  <img src="src/main/resources/images/logo.png" alt="TimeTable Logo" width="220" />
</p>

<p align="center">
  <strong>Solution moderne de conception, gestion et pilotage des emplois du temps d'établissements de formation</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-orange?logo=openjdk" alt="Java 25" />
  <img src="https://img.shields.io/badge/JavaFX-27-blue?logo=openjdk" alt="JavaFX 27" />
  <img src="https://img.shields.io/badge/Base_de_donn%C3%A9es-SQLite-003B57?logo=sqlite" alt="SQLite" />
  <img src="https://img.shields.io/badge/Plateformes-Windows%20%7C%20macOS%20%7C%20Linux-brightgreen" alt="Platforms" />
  <img src="https://img.shields.io/badge/Release-v2.0.0-blueviolet" alt="Version 2.0.0" />
</p>

---

## 📖 Présentation

**TimeTable** est une application bureautique complète conçue pour simplifier et optimiser la planification pédagogique au sein des centres et instituts de formation professionnelle et académique. 

Elle offre aux équipes de direction et coordinateurs pédagogiques une suite d'outils ergonomiques pour modéliser les contraintes, affecter les formateurs et les salles, générer les plannings sans conflit et suivre la volumétrie horaire en temps réel.

---

## ✨ Fonctionnalités Clés

### 📊 1. Tableau de bord de Direction & Indicateurs de Pilotage (KPIs)
- **Bannière institutionnelle dynamique** : identification claire de l'établissement, complexe, direction régionale, année de formation et projet actif.
- **Indicateurs stratégiques instantanés** :
  - Volume global d'heures planifiées et moyenne horaire par groupe.
  - Taux d'occupation global des salles et ateliers.
  - Effectif enseignant (ratio formateurs permanents vs vacataires, pourcentage de vacations).
  - Nombre total de groupes formés, filières et séances programmées.
- **Triple vue analytique en colonnes** :
  - **Formateurs** : liste complète, statuts, volumes horaires hebdomadaires cumulés et bouton d'ouverture directe de planning.
  - **Groupes** : liste détaillée par filière et promotion, masses horaires hebdomadaires et accès direct au planning.
  - **Répartition hebdomadaire par Jour & Totaux** : tableau condensé du lundi au dimanche affichant le total d'heures par jour, les groupes ayant cours, le nombre de séances, les formateurs mobilisés et une ligne de **TOTAL SEMAINE**.

### 🗓️ 2. Gestion Interactive des Emplois du Temps
- **Grille de planification visuelle** intuitive par créneaux horaires configurables.
- **Multi-vues synchronisées** :
  - Vue par Formateur.
  - Vue par Groupe d'étudiants.
  - Vue par Espace (salles de cours, laboratoires, ateliers).
- **Navigation contextuelle en un clic** : depuis les listes du tableau de bord, cliquez sur n'importe quel enseignant ou groupe pour ouvrir instantanément son onglet d'édition.
- **Détection proactive des conflits** : prévention des chevauchements d'horaires et de salles.

### 🏢 3. Gestion Pédagogique Intégrée
- **Formateurs** : fiche signalétique, statut (permanent / vacataire), affectation individualisée multi-groupes et suivi de charge.
- **Groupes & Filières** : structuration par filières, années de formation et divisions pédagogiques.
- **Espaces & Équipements** : inventaire des salles de cours magistraux, salles informatiques, ateliers techniques et capacités d'accueil.
- **Modules & Matières** : catalogue des modules enseignés et attributions par filière.

### 🖨️ 4. Impression & Persistance
- **Impression professionnelle** : aperçu avant impression et mise en page optimisée des fiches d'emploi du temps hebdomadaires (A4 paysage / portrait).
- **Format de fichier autonome** : stockage local rapide et intègre via SQLite sous format de fichier `.tk`.

---

## 🖥️ Téléchargement et Installation

Les installeurs autonomes intégrant un runtime Java optimisé (aucun prérequis Java chez l'utilisateur final) sont disponibles sur la page **[Releases GitHub](https://github.com/TAHRI-Khalid/Timetable/releases)** :

| Système | Architecture | Format d'installation |
| :--- | :--- | :--- |
| **Windows** | 64-bit (x64) | `.msi` (Installateur Windows natif) |
| **macOS** | Apple Silicon (M1 / M2 / M3 / M4) | `.dmg` (Package Apple Silicon arm64) |
| **Linux (Debian / Ubuntu / Mint)** | 64-bit (amd64) | `.deb` |
| **Linux (Fedora / RHEL / openSUSE)** | 64-bit (x86_64) | `.rpm` |
| **Universel (macOS Intel, Linux, Windows)** | Multi-plateforme | `TimeTable-2.0.0-all.jar` *(requiert Java 25)* |

---

## 🛠️ Compilation depuis les sources

### Prérequis
- **JDK 25** ou supérieur installé (ex: Eclipse Temurin ou OpenJDK 25).
- **Apache Maven 3.9+**.

### Étapes de construction

1. **Cloner le projet** :
   ```bash
   git clone https://github.com/TAHRI-Khalid/Timetable.git
   cd Timetable
   ```

2. **Compiler l'application** :
   ```bash
   mvn clean compile
   ```

3. **Lancer l'application en mode développement** :
   ```bash
   mvn javafx:run
   ```

4. **Générer le Fat JAR exécutable** :
   ```bash
   mvn package -DskipTests
   # Le fichier sera disponible dans target/TimeTable-2.0.0-all.jar
   # Exécution :
   java -jar target/TimeTable-2.0.0-all.jar
   ```

---

## 🚀 Automatisation CI/CD (GitHub Actions)

Le projet intègre un pipeline de publication automatique multi-OS situé dans [`.github/workflows/release.yml`](.github/workflows/release.yml).

À chaque nouveau tag de version (ex: `v2.0.0`) :
1. Une matrice compile l'application simultanément sur **Windows Server**, **Ubuntu** et **macOS (Intel & ARM)**.
2. `jlink` extrait un sous-ensemble minimal de modules Java.
3. `jpackage` assemble les installeurs natifs (`.msi`, `.deb`, `.rpm`, `.dmg`).
4. La GitHub Release est automatiquement créée et enrichie de tous les livrables binaires.

---

## 👤 Auteur

- **TAHRI Khalid** - *Conception et Développement* - [GitHub](https://github.com/TAHRI-Khalid)

---

## 📄 Licence

Ce projet est sous licence propriétaire / réservée à l'auteur. Tous droits réservés.
