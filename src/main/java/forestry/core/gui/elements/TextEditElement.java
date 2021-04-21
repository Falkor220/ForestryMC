package forestry.core.gui.elements;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import com.mojang.blaze3d.matrix.MatrixStack;

import forestry.core.gui.elements.lib.IValueElement;
import forestry.core.gui.elements.lib.events.ElementEvent;
import forestry.core.gui.elements.lib.events.GuiEvent;
import forestry.core.gui.elements.lib.events.GuiEventDestination;
import forestry.core.gui.elements.lib.events.TextEditEvent;

public class TextEditElement extends GuiElement implements IValueElement<String> {

	private final TextFieldWidget field;

	public TextEditElement(int xPos, int yPos, int width, int height) {
		super(xPos, yPos, width, height);
		field = new TextFieldWidget(Minecraft.getInstance().font, 0, 0, width, height, StringTextComponent.EMPTY);
		field.setBordered(false);
		this.addSelfEventHandler(GuiEvent.KeyEvent.class, event -> {
			String oldText = field.getValue();
			this.field.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
			final String text = field.getValue();
			if (!text.equals(oldText)) {
				postEvent(new TextEditEvent(this, text, oldText), GuiEventDestination.ALL);
			}
		});
		this.addSelfEventHandler(GuiEvent.CharEvent.class, event -> {
			String oldText = field.getValue();
			this.field.charTyped(event.getCharacter(), event.getModifiers());
			final String text = field.getValue();
			if (!text.equals(oldText)) {
				postEvent(new TextEditEvent(this, text, oldText), GuiEventDestination.ALL);
			}
		});
		this.addSelfEventHandler(GuiEvent.DownEvent.class, event -> {
			Window windowElement = getWindow();
			this.field.mouseClicked(windowElement.getRelativeMouseX(this), windowElement.getRelativeMouseY(this), event.getButton());
		});
		//TODO - method protected so maybe AT the field itself?
		this.addSelfEventHandler(ElementEvent.GainFocus.class, event -> this.field.setFocus(true));
		this.addSelfEventHandler(ElementEvent.LoseFocus.class, event -> this.field.setFocus(false));
	}

	public TextEditElement setMaxLength(int maxLength) {
		field.setMaxLength(maxLength);
		return this;
	}

	public TextEditElement setValidator(Predicate<String> validator) {
		field.setFilter(validator);
		return this;
	}

	@Override
	public String getValue() {
		return field.getValue();
	}

	@Override
	public void setValue(String value) {
		if (!field.getValue().equals(value)) {
			field.setValue(value);
		}
	}

	//TODO - maybe need to supply start/end points now?
	//TODO third param probably partial ticks. Is it being 0 a problem?
	@Override
	public void drawElement(MatrixStack transform, int mouseX, int mouseY) {
		field.render(transform, mouseY, mouseX, 0);
	}

	@Override
	public boolean canFocus() {
		return true;
	}

	@Override
	public boolean canMouseOver() {
		return true;
	}
}
