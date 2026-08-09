# Connectable Blocks

This document is the authoritative design note for connectable blocks (wires and future pipes/logistics pipes). Read it
before modifying `IConnectable`, `IConnectableBlock`,
`WireBlock`, `WireBlockEntity`, or before adding a new connectable transport block.

## Maintenance rules

- Any modification to a connectable block class must be explained to the user and approved before it is applied.
- Any change that affects connection behavior, interfaces, placement/update rules, or models must also be reflected in
  this document so it stays accurate.

## Layering

- `core/logistic/IConnectable`: pure connection judgment only. No block collision or block state. Provides
  `canConnectTo`, `connections`, `isConnectTo`, and `isConnectWith`.
- `block/IConnectableBlock`: block-layer connection state, placement/update rules, and dynamic shape/collision contract.
- Inheritance:
    - `WireBlock -> IConnectableBlock -> IConnectable`
    - `WireBlockEntity -> IWire -> IEnergyComponent -> IConnectable`

## Mandatory connection rules

1. Never auto-connect to all neighbors when a block is placed. Only connect:
    - the clicked contact face when placed onto a compatible connectable or energy capability block, and
    - neighbors that already have a connection face pointing at the new position.
2. Neighbor updates only change the contact face, never recompute all six faces.
3. Automatic updates only establish connections; they must never disconnect. Destroying a connectable leaves the
   neighbor connection states unchanged.
4. Manual disconnects persist in the block entity and must be respected:
   `canAutoConnectTo = canConnectTo && !isConnectionDisabled`.
5. `canConnectTo` is the compatibility/extension point. New transport types override it and decide which neighbors are
   compatible.
6. When a block is placed, the new block state comes from `getPlacementConnectionState`; the existing neighbor updates
   itself through `updateShape -> updateConnectionState`.
7. Notify the energy/logistic network only after the state is actually applied.
   `WireBlock.onBlockStateChange` is the server-side hook that calls `onConnectionChanged`.

## Model and data

- Prefer datagen for blockstates/models; handwritten JSON is allowed only for custom model templates.
- The wire item hand model is currently adjusted manually by the user. Do not overwrite those manual model tweaks unless
  the user explicitly asks.
