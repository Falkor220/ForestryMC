/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.core.owner;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.DataInputStreamForestry;
import forestry.core.network.DataOutputStreamForestry;
import forestry.core.network.IStreamable;
import forestry.core.utils.PlayerUtil;
import net.minecraft.nbt.NBTTagCompound;

public class OwnerHandler implements IOwnerHandler, IStreamable, INbtWritable, INbtReadable {
	private GameProfile owner = null;

	@Override
	public GameProfile getOwner() {
		return owner;
	}

	@Override
	public void setOwner(@Nonnull GameProfile owner) {
		this.owner = owner;
	}

	@Override
	public void writeData(DataOutputStreamForestry data) throws IOException {
		if (owner == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeLong(owner.getId().getMostSignificantBits());
			data.writeLong(owner.getId().getLeastSignificantBits());
			data.writeUTF(owner.getName());
		}
	}

	@Override
	public void readData(DataInputStreamForestry data) throws IOException {
		if (data.readBoolean()) {
			GameProfile owner = new GameProfile(new UUID(data.readLong(), data.readLong()), data.readUTF());
			setOwner(owner);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		if (data.hasKey("owner")) {
			GameProfile owner = PlayerUtil.readGameProfileFromNBT(data.getCompoundTag("owner"));
			if (owner != null) {
				setOwner(owner);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		if (this.owner != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			PlayerUtil.writeGameProfile(nbt, owner);
			data.setTag("owner", nbt);
		}
		return data;
	}
}