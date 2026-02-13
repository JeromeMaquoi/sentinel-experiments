# Sentinel Experiments

This project is a pipeline-based tool for analyzing energy consumption in Java projects. It combines two powerful approaches: **energy measurement** using JoularJX and **source code instrumentation** using Spoon.

## Project Architecture Overview

The project operates through a **modular pipeline architecture** consisting of two main modules:

1. **EnergyMeasurementsModule** - Measures actual energy consumption of Java applications
2. **SpoonInstrumentConstructorModule** - Instruments source code to enable detailed analysis

Each module executes a series of stages sequentially to accomplish its goals.

---

## Module 1: EnergyMeasurementsModule

### Purpose
Measures the energy consumption of a Java project by:
- Cloning and setting up the target repository
- Building and preparing the project
- Running the project multiple times with energy measurement tools
- Collecting and processing energy measurement results

### How It Works

The module executes stages in three phases:

#### Phase 1: Setup & Preparation
```
CloneAndCheckoutRepositoryStage
    ↓
Tool-specific Setup Stages
```

**CloneAndCheckoutRepositoryStage**
- Clones the target repository from Git
- Checks out the specified branch/tag/commit
- Prepares the project directory for measurement

**Tool-specific Setup Stages** (created by the EnergyMeasurementTool)
- Installs and configures the energy measurement tool (e.g., JoularJX)
- Prepares the environment for measurements
- Validates tool installation

#### Phase 2: Measurement (Repeated for each test run)
```
Tool Measurement Stages [Run 1]
    ↓
Tool Measurement Stages [Run 2]
    ↓
... (repeated N times based on configuration)
```

Each measurement stage executes:
- Building the project
- Running tests or application with energy monitoring enabled
- Collecting raw energy consumption data

#### Phase 3: Post-Processing
```
JoularJX Results Processing Stage
    ↓
Results Analysis & Aggregation
```

**JoularJX Results Processing Stage**
- Reads JoularJX output files from the `joularjx-results` directory
- Parses energy consumption data organized by:
  - **Scope**: `all` (all methods including JDK) vs `app` (application methods only)
  - **Timing**: `runtime` (per-second measurements) vs `total` (aggregated totals)
  - **Type**: `calltree` (call hierarchy) vs `methods` (individual methods)
- Structures the data into domain objects
- Stores results in the database

### Configuration

The number of test runs is configured via:
```java
config.getExecutionPlan().getNumTestRuns()
```

The energy measurement tool is configured via:
```java
config.getExecutionPlan().getEnergyMeasurements().getTool()
```

---

## Module 2: SpoonInstrumentConstructorModule

### Purpose
Instruments Java source code to track constructor invocations by:
- Cloning and preparing the target repository
- Building a classpath for analysis
- Using Spoon to instrument all constructors
- Running tests on the instrumented code
- Storing instrumentation results

### How It Works

The module executes the following stages in sequence:

#### Stage Breakdown

```
1. StopBackendStage
   └─ Stops any running backend services and clears previous data

2. PrepareBackendStage
   └─ Starts MongoDB and backend services needed for instrumentation tracking

3. CloneAndCheckoutRepositoryStage
   └─ Clones the target repository and checks out the specified version

4. CopyDirectoryStage
   └─ Creates necessary working directories for instrumentation

5. CopyFileStage (Classpath)
   └─ Copies classpath build files (build.gradle, build.gradle.kts, or pom.xml)
   └─ Used to resolve dependencies without instrumenting

6. BuildClassPathStage
   └─ Builds the project to generate classpath
   └─ Allows Spoon to understand the full dependency graph

7. CopyFileStage (Instrumentation)
   └─ Copies instrumentation-specific build files
   └─ May contain custom gradle/maven tasks for instrumentation

8. InstrumentConstructorsStage
   └─ Uses Spoon to analyze the codebase
   └─ Instruments every constructor with tracking code
   └─ Generates modified source code and bytecode

9. CopySourceCodeStage
   └─ Copies original source code for reference and comparison

10. CopyProjectJavaFilesStage
    └─ Copies all Java files from the project

11. RunInstrumentedProjectTestsStage
    └─ Runs the project's test suite on the instrumented code
    └─ Collects constructor invocation data via backend services
    └─ Sends data to MongoDB for storage and analysis
```

### Key Concepts

**Build Files Strategy**
- The module uses two sets of build files:
  - **Classpath build files**: For dependency resolution without instrumentation
  - **Instrumentation build files**: Custom build configurations for the instrumented code

**Spoon Instrumentation**
- Analyzes the complete source code using the classpath context
- Instruments constructors to track:
  - When they are called
  - How often they are called
  - The call hierarchy
- Generates both source and bytecode for the instrumented version

**Backend Integration**
- MongoDB stores all instrumentation data
- Backend services receive constructor invocation events during test execution
- Data is persisted for later analysis

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Start Pipeline Execution                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
        ┌───────▼─────────┐    ┌──────────▼──────────┐
        │ Energy          │    │ Spoon              │
        │ Measurements    │    │ Instrumentation    │
        │ Module          │    │ Module             │
        └───────┬─────────┘    └──────────┬──────────┘
                │                         │
        ┌───────▼─────────┐    ┌──────────▼──────────┐
        │ 1. Clone Repo   │    │ 1. Stop Backend    │
        │ 2. Setup Tool   │    │ 2. Prepare Backend │
        │ 3. Measure (×N) │    │ 3. Clone Repo      │
        │ 4. Post-process │    │ 4. Build Classpath │
        └───────┬─────────┘    │ 5. Instrument Code │
                │              │ 6. Run Tests       │
        ┌───────▼─────────┐    └──────────┬──────────┘
        │ Energy Data     │              │
        │ (Database)      │    ┌──────────▼──────────┐
        │                 │    │ Constructor Calls   │
        │                 │    │ (MongoDB)           │
        └─────────────────┘    └─────────────────────┘
```

---

## Configuration Files

This section explains how to create configuration files for new projects.

### Configuration File Naming Convention

- **Work in Progress**: `wip-config-<PROJECT_NAME>.yml` (e.g., `wip-config-jabref.yml`)
  - Use this while developing and testing your configuration
  - These files are not yet ready for server execution

- **Complete & Tested**: `config-<PROJECT_NAME>.yml` (e.g., `config-spring-boot.yml`)
  - Use this when your configuration is validated and working
  - These files are ready for execution on the analysis server

### Configuration File Structure

Here's a complete example configuration with explanations:

```yaml
# config-<PROJECT_NAME>.yml

# ============================================================================
# GENERAL SETTINGS
# ============================================================================

# Maximum execution time (in seconds) for the entire pipeline
command-time-out: 3600

# Path to the Spoon constructor instrumentation code
code-constructors-instrumentation-path: "/path/to/sentinel-experiments/src/main/java/be/unamur/snail/spoon/constructor_instrumentation"

# ============================================================================
# PROJECT INFORMATION
# ============================================================================

project:
  # Human-readable project name (used in logs and reporting)
  name: "my-project"
  
  # GitHub username/organization that owns the repository
  owner: "github-username"
  
  # Sub-project path (if the repository contains multiple projects)
  # Leave empty string "" if this is a single-project repository
  sub-project: "optional-subdirectory"
  
  # Display detailed project logs during execution
  show-project-logs: true
  
  # Java package prefix to filter classes for Spoon instrumentation
  # Only classes matching this prefix will be instrumented
  # Example: "org.springframework" instruments all org.springframework.* classes
  package-prefix: "com.mycompany"

# ============================================================================
# REPOSITORY CONFIGURATION
# ============================================================================

repo:
  # GitHub repository URL
  url: "https://github.com/owner/project"
  
  # Git commit hash to analyze (not branch/tag, must be a specific commit)
  # Use a commit hash to ensure reproducible analysis across different server runs
  commit: "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
  
  # Local directory where the repository will be cloned
  # Must be writable and have sufficient space (varies by project size)
  target-dir: "/tmp/sentinel-analysis/my-project"
  
  # JDK version to use for building and testing
  # Must be compatible with the project
  # Use SDKMAN version format (e.g., "17.0.17-tem", "21.0.1-temurin")
  jdk: "17.0.17-tem"
  
  # Whether to delete and re-clone the repository if it already exists locally
  overwrite-clone: false
  
  # Whether to overwrite the copy directory used for analysis
  overwrite-copy: true

# ============================================================================
# LOGGING CONFIGURATION
# ============================================================================

log:
  # Logging level: TRACE, DEBUG, INFO, WARN, ERROR
  level: INFO
  
  # Directory where logs will be stored
  directory: "/tmp/sentinel-logs/my-project/"
  
  # Also print logs to console (useful for monitoring execution)
  also-log-to-console: true
  
  # Delete previous logs from this project when starting a new run
  clear-previous-logs: true

# ============================================================================
# EXECUTION PLAN
# ============================================================================

execution-plan:
  # Command to run tests (must be appropriate for the build system)
  # Examples:
  #   - Maven: "mvn clean test"
  #   - Gradle: "./gradlew clean test"
  #   - Gradle with sub-project: "./gradlew clean my-project:test"
  test-command: "./gradlew clean test"
  
  # Whether to continue execution if tests fail
  # Set to true if you want to complete analysis even with failing tests
  ignore-failures: false
  
  # Whether to continue if Spoon instrumentation fails
  # Set to true to skip Spoon module if it encounters issues
  ignore-spoon-failures: true
  
  # Number of times to repeat the energy measurement
  # Higher values provide more reliable energy data but take longer
  # Recommended: 3-5 for reliable results
  num-test-runs: 1
  
  # Energy measurement configuration
  energy-measurements:
    # Tool to use for energy measurement (currently only "joularjx" is supported)
    tool: "joularjx"
    
    # JoularJX version
    tool-version: "3.0.1"
    
    # URL to download the tool (used if not already installed)
    release-url: "https://github.com/joular/joularjx"
    
    # Local path where JoularJX is installed
    tool-path: "/path/to/joularjx"
    
    # Configuration for which data to import from JoularJX results
    import-config:
      # Measurement scopes
      # "app": only application code (filtered by package-prefix)
      # "all": all code including JDK
      scopes: ["app"]
      
      # Measurement timing types
      # "runtime": per-second measurements during execution
      # "total": aggregated total energy consumption
      measurement-types: ["runtime", "total"]
      
      # Method grouping types
      # "calltrees": organize by call hierarchy
      # "methods": individual method measurements
      monitoring-types: ["calltrees", "methods"]

# ============================================================================
# BACKEND CONFIGURATION (for Spoon instrumentation)
# ============================================================================

backend:
  # Execution mode: "dev" or "prod"
  # "dev": backend runs on the same machine (localhost)
  # "prod": backend runs on a remote server
  mode: dev
  
  # Backend server hostname or IP
  server-host: localhost
  
  # Backend server port
  server-port: 8080
  
  # Timeout for waiting for backend responses (in seconds)
  server-timeout-seconds: 120
  
  # Path where backend logs are written
  server-log-path: "/tmp/sentinel-backend.log"
  
  # File that signals backend is ready (created by backend when startup completes)
  server-ready-path: "/tmp/backend-ready"
  
  # API endpoint for sending constructor invocation data
  endpoint: "/api/v2/constructor-contexts"
  
  # ========== Development Mode Settings ==========
  
  # Path to the backend source code (only used in dev mode)
  # The backend will be started from this location
  server-path: "/path/to/sentinel-backend/"
  
  # Number of checks to perform when waiting for backend startup
  # Each check waits 1 second, so this value is the total startup wait time
  nb-check-server-start: 20
  
  # ========== Production Mode Settings ==========
  
  # SSH username for connecting to remote server (only used in prod mode)
  ssh-user: "username"
  
  # SSH hostname/IP of remote server (only used in prod mode)
  ssh-host: "server.example.com"
```

---

## Projects Status Overview

| Project Name              | JoularJX Status | Spoon Status   | Remaining Configuration TODOs |
|---------------------------|:---------------:|----------------|-------------------------------|
| **checkstyle**            |   🟢 Working    | 🟢 Working     | /                             |
| **commons-configuration** |    🔵 To Do     | 🔵 To Do       |                               |
| **hibernate-orm**         |    🔵 To Do     | 🟡 In Progress |                               |
| **jabref**                | 🟡 In Progress  | 🔵 To Do       |                               |
| **OpenJDK**               |    🔵 To Do     | 🔵 To Do       |                               |
| **spoon**                 |   🟢 Working    | 🟡 In Progress |                               |
| **spring-boot**           |   🟢 Working    | 🟢 Working     | /                             |

---

## Status Legend

| Icon               | Meaning                                                                                          |
|--------------------|--------------------------------------------------------------------------------------------------|
| 🟢 **Working**     | Fully configured and running successfully within the pipeline                                    |
| 🟡 **In Progress** | Configuration in progress, partial setup completed, requires further adjustments                 |
| 🔴 **Not Working** | Fails during setup or measurement stages, too difficult to analyze. The project will not be used |
| 🔵 **To Do**       | Project not yet configured or analyzed                                                           |
