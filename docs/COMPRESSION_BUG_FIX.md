# Critical Compression Bug Fix

## Problem

There was a critical bug in the compressed backup system where all blocks were being incorrectly stored with the same palette key (`b0`), causing:

1. **Zero block count** being reported for compressed backups on server start
2. **Incorrect restoration** where all blocks were restored as the same type
3. **Data corruption** in compressed backup files

## Root Cause

The `createBlockSignature()` method in `FileManager.java` was using incorrect key names:

- **Looking for**: `"type"`, `"data"`, `"blockData"`
- **Actually stored**: `"material"`, `"blockDataString"`

This caused all blocks to have the same signature (`"null"`), so they all got assigned to palette entry `b0`.

## Solution

Fixed the `createBlockSignature()` method to:

1. **Use correct key names**: `"material"` instead of `"type"`, `"blockDataString"` instead of `"blockData"`
2. **Include special data**: Banner patterns, sign lines, container contents, skull data, NBT data, etc.
3. **Generate unique signatures**: Ensures different block types and states get different palette entries

## Result

- **Proper palette optimization**: Different block types now get assigned to different palette entries (`b0`, `b1`, `b2`, etc.)
- **Accurate block counts**: Compressed backups now show correct block counts
- **Correct restoration**: All blocks are restored with their proper types and properties
- **Better compression**: More efficient storage for areas with many different block types

## Technical Details

The fix ensures that the block signature accurately represents the uniqueness of each block, taking into account:

- Material type
- Block data string (orientation, properties, etc.)
- Special properties (banners, signs, containers, skulls, NBT data)

This prevents the palette optimization from incorrectly grouping different blocks together.
