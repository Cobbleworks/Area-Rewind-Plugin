# WorldEdit Selection Integration Improvement

## Problem Addressed

Previously, the Area Rewind plugin only properly handled basic cuboid WorldEdit selections. However, WorldEdit supports multiple selection modes:

- **Cuboid** (`//sel cuboid`) - Basic rectangular selection
- **Extend** (`//sel extend`) - Right-click extends the selection
- **Polygon** (`//sel poly`) - 2D polygonal selection
- **Ellipsoid** (`//sel ellipsoid`) - Elliptical/spherical selection
- **Cylinder** (`//sel cyl`) - Cylindrical selection
- **Convex Hull** (`//sel convex`) - Convex polyhedral selection

The plugin's core design is based on rectangular areas defined by two corner points (pos1 and pos2), which works perfectly for backups and area protection. However, when users made complex WorldEdit selections (like polygons or ellipsoids), the plugin would either fail to sync properly or create incorrect rectangular areas.

## Solution Implemented

### 1. Robust Region Conversion

The updated `syncWorldEditSelection()` method now:

- **Accepts any WorldEdit region type** - No longer limited to cuboids
- **Calculates proper bounding box** - Uses `region.getMinimumPoint()` and `region.getMaximumPoint()` to get the rectangular bounds
- **Preserves selection intent** - The bounding box encompasses the entire original selection
- **Validates selection size** - Prevents huge selections that could cause performance issues

### 2. User Feedback & Transparency

When a non-rectangular selection is converted:

```
[Player] Converted Polygon selection to rectangular area!
[Player] Original: 1,234 blocks → Bounding box: 2,400 blocks
[Player] Synced Polygon selection (1,234 blocks)
```

This tells users:

- What type of selection they made
- How many blocks were in the original selection
- How many blocks are in the converted rectangular area
- That the conversion was successful

### 3. Selection Type Detection

Added `getRegionTypeName()` method that identifies:

- CuboidRegion → "Cuboid"
- Polygonal2DRegion → "Polygon"
- EllipsoidRegion → "Ellipsoid"
- CylinderRegion → "Cylinder"
- ConvexPolyhedralRegion → "Convex Hull"
- ExtendingCuboidRegion → "Extended Cuboid"

### 4. Improved UI Messages

Updated various user-facing messages:

- **Welcome message** - Clarifies that any WorldEdit selection type works
- **Selection tool lore** - Mentions support for all selection types
- **Help text** - Better explains WorldEdit integration

## Technical Implementation

### Key Changes in `PlayerInteractionListener.java`

1. **Enhanced syncWorldEditSelection()** - Handles all region types
2. **Added getRegionTypeName()** - Identifies selection types for user feedback
3. **Added formatNumber()** - Formats large numbers with commas for readability
4. **Updated user messages** - More accurate and informative

### How It Works

1. **User makes any WorldEdit selection** (polygon, ellipsoid, etc.)
2. **Plugin detects WorldEdit wand interaction**
3. **Retrieves the WorldEdit Region object** of any type
4. **Calculates bounding box** using `getMinimumPoint()` and `getMaximumPoint()`
5. **Sets pos1 and pos2** to the bounding box corners
6. **Provides user feedback** about the conversion
7. **Area is ready** for backup/protection operations

### Benefits

- ✅ **Works with all WorldEdit selection modes**
- ✅ **Transparent conversion process**
- ✅ **Maintains plugin's rectangular area model**
- ✅ **Preserves all area functionality** (backups, visualization, etc.)
- ✅ **Clear user feedback** about what happened
- ✅ **Prevents confusion** about selection differences

## Example Usage Scenarios

### Scenario 1: Polygon Selection

```
1. Player uses //sel poly
2. Player selects a complex building outline
3. Plugin converts polygon to rectangular bounding box
4. User sees: "Converted Polygon selection to rectangular area!"
5. Area protection covers the entire building plus some buffer space
```

### Scenario 2: Ellipsoid Selection

```
1. Player uses //sel ellipsoid
2. Player selects a spherical area around a structure
3. Plugin creates rectangular area encompassing the sphere
4. Backup operations work normally on the rectangular region
```

### Scenario 3: Extend Mode

```
1. Player uses //sel extend
2. Player extends selection by right-clicking multiple times
3. Plugin automatically syncs the final extended selection
4. Rectangular area matches the extended bounds
```

## Backward Compatibility

This change is fully backward compatible:

- **Existing cuboid selections** work exactly as before
- **No breaking changes** to the API or data structures
- **All existing features** continue to work normally
- **Performance** is maintained or improved

## Future Enhancements

Potential future improvements could include:

- **Selection preview** showing the rectangular conversion before saving
- **Advanced conversion options** (e.g., padding around complex selections)
- **Selection history** to track recent WorldEdit selections
- **Integration with WorldEdit brushes** for dynamic area updates
