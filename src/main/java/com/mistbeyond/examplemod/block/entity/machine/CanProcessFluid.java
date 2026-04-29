package com.mistbeyond.examplemod.block.entity.machine;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public interface CanProcessFluid {
    StacksResourceHandler<FluidStack, FluidResource> getFluidHandler();
}
