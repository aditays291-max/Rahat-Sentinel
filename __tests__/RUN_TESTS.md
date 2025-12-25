# Running the Generated Tests

## Quick Start

```bash
# Run all tests
npm test

# Run with coverage report
npm test -- --coverage

# Run in watch mode (auto-rerun on file changes)
npm test -- --watch
```

## Run Specific Test Suites

### Location Awareness Tests
```bash
# All location awareness tests
npm test features/locationAwareness

# Just location context
npm test locationContext.test.ts

# Just relevance engine
npm test relevanceEngine.test.ts
```

### Android Platform Tests
```bash
# All Android notification tests
npm test platform/android

# Individual test files
npm test backgroundNotifier.test.ts
npm test foregroundNotifier.test.ts
npm test registerBackgroundHandler.test.ts
```

## Run Tests with Specific Options

```bash
# Run only failed tests from previous run
npm test -- --onlyFailures

# Run tests matching a pattern
npm test -- -t "should handle"

# Run tests and generate coverage report
npm test -- --coverage --coverageDirectory=coverage

# Run with verbose output
npm test -- --verbose

# Run a single test file
npm test __tests__/features/locationAwareness/locationContext.test.ts
```

## Expected Output

All tests should pass with output similar to: