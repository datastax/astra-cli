# Unit Test Plan for CommonOptions.merge()

## Current Status

The user has added `@AllArgsConstructor` and `@NoArgsConstructor` to `CommonOptions.java`. The merge() method implementation is currently commented out and needs to be updated to use the AllArgsConstructor.

## Task Breakdown

### 1. Update merge() method (Code Mode Required)
The merge method needs to be rewritten to use the AllArgsConstructor:

```java
public CommonOptions merge(CommonOptions other) {
    return new CommonOptions(
        (this.ansi.isPresent()) ? this.ansi : other.ansi,
        (this.outputType.isPresent()) ? this.outputType : other.outputType,
        (this.verbose.isPresent()) ? this.verbose : other.verbose,
        (this.quiet.isPresent()) ? this.quiet : other.quiet,
        (this.enableSpinner.isPresent()) ? this.enableSpinner : other.enableSpinner,
        (this.shouldDumpLogs.isPresent()) ? this.shouldDumpLogs : other.shouldDumpLogs,
        (this.dumpLogsTo.isPresent()) ? this.dumpLogsTo : other.dumpLogsTo,
        (this.noInput.isPresent()) ? this.noInput : other.noInput
    );
}
```

### 2. Create Unit Tests (Code Mode Required)

**File Location**: `src/test/java/com/dtsx/astra/cli/unit/commands/CommonOptionsTest.java`

**Test Structure**:
```java
package com.dtsx.astra.cli.unit.commands;

import com.dtsx.astra.cli.commands.CommonOptions;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import net.jqwik.api.*;
import picocli.CommandLine.Help.Ansi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Group
public class CommonOptionsTest {
    
    @Group
    class MergeTests {
        
        @Property
        void empty_merge_with_empty_returns_empty() {
            // Both empty
            val empty1 = new CommonOptions();
            val empty2 = new CommonOptions();
            
            val result = empty1.merge(empty2);
            
            assertThat(result.ansi()).isEmpty();
            assertThat(result.enableSpinner()).isEmpty();
            assertThat(result.dumpLogsTo()).isEmpty();
        }
        
        @Property
        void this_takes_precedence_over_other_for_ansi(@ForAll boolean useThis) {
            val thisOpt = new CommonOptions(
                Optional.of(Ansi.ON),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            val otherOpt = new CommonOptions(
                Optional.of(Ansi.OFF),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            
            val result = thisOpt.merge(otherOpt);
            
            assertThat(result.ansi()).contains(Ansi.ON);
        }
        
        @Property
        void uses_other_when_this_is_empty_for_ansi() {
            val thisOpt = new CommonOptions(
                Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            val otherOpt = new CommonOptions(
                Optional.of(Ansi.OFF),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            
            val result = thisOpt.merge(otherOpt);
            
            assertThat(result.ansi()).contains(Ansi.OFF);
        }
        
        // Similar tests for each field:
        // - outputType
        // - verbose
        // - quiet
        // - enableSpinner
        // - shouldDumpLogs
        // - dumpLogsTo
        // - noInput
        
        @Property
        void all_fields_present_in_this_returns_this_values() {
            val thisOpt = new CommonOptions(
                Optional.of(Ansi.ON),
                Optional.of(OutputType.JSON),
                Optional.of(true),
                Optional.of(false),
                Optional.of(true),
                Optional.of(true),
                Optional.of(Paths.get("/tmp/logs")),
                Optional.of(false)
            );
            val otherOpt = new CommonOptions(
                Optional.of(Ansi.OFF),
                Optional.of(OutputType.CSV),
                Optional.of(false),
                Optional.of(true),
                Optional.of(false),
                Optional.of(false),
                Optional.of(Paths.get("/other/logs")),
                Optional.of(true)
            );
            
            val result = thisOpt.merge(otherOpt);
            
            assertThat(result.ansi()).contains(Ansi.ON);
            assertThat(result.outputType()).isEqualTo(OutputType.JSON);
            assertThat(result.verbose()).isTrue();
            assertThat(result.quiet()).isFalse();
            assertThat(result.enableSpinner()).contains(true);
            assertThat(result.shouldDumpLogs()).isTrue();
            assertThat(result.dumpLogsTo()).contains(Paths.get("/tmp/logs"));
            assertThat(result.noInput()).isFalse();
        }
        
        @Property
        void all_fields_empty_in_this_returns_other_values() {
            val thisOpt = new CommonOptions();
            val otherOpt = new CommonOptions(
                Optional.of(Ansi.OFF),
                Optional.of(OutputType.CSV),
                Optional.of(false),
                Optional.of(true),
                Optional.of(false),
                Optional.of(false),
                Optional.of(Paths.get("/other/logs")),
                Optional.of(true)
            );
            
            val result = thisOpt.merge(otherOpt);
            
            assertThat(result.ansi()).contains(Ansi.OFF);
            assertThat(result.outputType()).isEqualTo(OutputType.CSV);
            assertThat(result.verbose()).isFalse();
            assertThat(result.quiet()).isTrue();
            assertThat(result.enableSpinner()).contains(false);
            assertThat(result.shouldDumpLogs()).isFalse();
            assertThat(result.dumpLogsTo()).contains(Paths.get("/other/logs"));
            assertThat(result.noInput()).isTrue();
        }
        
        @Property
        void merge_does_not_modify_original_instances() {
            val thisOpt = new CommonOptions(
                Optional.of(Ansi.ON),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            val otherOpt = new CommonOptions(
                Optional.of(Ansi.OFF),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            );
            
            val result = thisOpt.merge(otherOpt);
            
            // Verify originals unchanged
            assertThat(thisOpt.ansi()).contains(Ansi.ON);
            assertThat(otherOpt.ansi()).contains(Ansi.OFF);
            assertThat(result.ansi()).contains(Ansi.ON);
        }
    }
}
```

## Test Coverage Summary

### Fields to Test (8 total):
1. ✓ ansi - Optional<Ansi>
2. ✓ outputType - Optional<OutputType>
3. ✓ verbose - Optional<Boolean>
4. ✓ quiet - Optional<Boolean>
5. ✓ enableSpinner - Optional<Boolean>
6. ✓ shouldDumpLogs - Optional<Boolean>
7. ✓ dumpLogsTo - Optional<Path>
8. ✓ noInput - Optional<Boolean>

### Test Scenarios:
- Empty merge with empty
- Precedence: this over other (for each field)
- Fallback: use other when this is empty (for each field)
- All fields present in this
- All fields empty in this
- Immutability verification

### Estimated Test Count: ~25-30 test methods

## Next Steps

1. **Switch to Code Mode** - Plan mode cannot edit Java files
2. **Update merge() method** - Implement using AllArgsConstructor
3. **Create test file** - Write comprehensive unit tests
4. **Run tests** - Verify all tests pass
5. **Review coverage** - Ensure all merge logic is tested

## Success Criteria
- ✓ merge() method uses AllArgsConstructor
- ✓ All 8 fields tested for merge behavior
- ✓ Tests follow project conventions (jqwik, AssertJ)
- ✓ Tests compile and pass
- ✓ Immutability verified
