/*
 * Copyright 2018-2020 Florian Spie�
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package webhook.send.component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Button components that can be placed inside a {@link LayoutComponent}
 *
 * @see LayoutComponent#addComponent(ActionComponent)
 */
public class Button implements ActionComponent, SingleEmojiContainer<Button> {

	public static final int MAX_BUTTONS = 5;

	private final Style style;
	private final String label, customId, url;
	private boolean disabled;
	private PartialEmoji emoji;

	private Button(@NotNull Style style, @NotNull String label, @Nullable String customId, @Nullable String url, boolean disabled) {
		this.style = style;
		this.label = label;
		this.customId = customId;
		this.url = url;
		this.disabled = disabled;
	}

	/**
	 * A button with style set to {@link Style#PRIMARY}
	 *
	 * @param  customId
	 *         Custom id used for handling of interactions
	 * @param  label
	 *         Label used to display text on a button
	 *
	 * @return A primary style button with the provided id and label
	 */
	@NotNull
	public static Button primary(@NotNull String customId, @NotNull String label) {
		return new Button(Style.PRIMARY, label, customId, null, false);
	}

	@NotNull
	public static Button primary(@NotNull String customId, @NotNull String label, boolean disabled) {
		return new Button(Style.PRIMARY, label, customId, null, disabled);
	}

	/**
	 * A button with style set to {@link Style#SUCCESS}
	 *
	 * @param  customId
	 *         Custom id used for handling of interactions
	 * @param  label
	 *         Label used to display text on a button
	 *
	 * @return A primary style button with the provided id and label
	 */
	@NotNull
	public static Button success(@NotNull String customId, @NotNull String label) {
		return new Button(Style.SUCCESS, label, customId, null, false);
	}

	/**
	 * A button with style set to {@link Style#SECONDARY}
	 *
	 * @param  customId
	 *         Custom id used for handling of interactions
	 * @param  label
	 *         Label used to display text on a button
	 *
	 * @return A primary style button with the provided id and label
	 */
	@NotNull
	public static Button secondary(@NotNull String customId, @NotNull String label) {
		return new Button(Style.SECONDARY, label, customId, null, false);
	}

	@NotNull
	public static Button secondary(@NotNull String customId, @NotNull String label, boolean disabled) {
		return new Button(Style.SECONDARY, label, customId, null, disabled);
	}

	/**
	 * A button with style set to {@link Style#DANGER}
	 *
	 * @param  customId
	 *         Custom id used for handling of interactions
	 * @param  label
	 *         Label used to display text on a button
	 *
	 * @return A primary style button with the provided id and label
	 */
	@NotNull
	public static Button danger(@NotNull String customId, @NotNull String label) {
		return new Button(Style.DANGER, label, customId, null, false);
	}

	/**
	 * A button with style set to {@link Style#LINK}
	 *
	 * @param  url
	 *         URL to link the user to
	 * @param  label
	 *         Label used to display text on a button
	 *
	 * @return A primary style button with the provided id and label
	 */
	@NotNull
	public static Button link(@NotNull String url, @NotNull String label) {
		return new Button(Style.LINK, label, null, url, false);
	}

	/**
	 * @return The style of the button
	 */
	@NotNull
	public Style getStyle() {
		return style;
	}

	/**
	 * @return The label/button text of the button
	 */
	@NotNull
	public String getLabel() {
		return label;
	}

	/**
	 * @return The dev-defined id of the button
	 */
	@Nullable
	@Override
	public String getCustomId() {
		return customId;
	}

	/**
	 * @return The url the button links to
	 */
	@Nullable
	public String getUrl() {
		return url;
	}

	@Override
	public String toJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", this.getType().getId());
		json.put("style", this.style.value);
		json.put("label", this.label);
		if (this.customId != null)
			json.put("custom_id", this.customId);
		if (this.url != null)
			json.put("url", this.url);
		json.put("disabled", this.disabled);
		json.put("emoji", this.emoji);
		return json.toString();
	}

	@NotNull
	@Override
	public Type getType() {
		return Type.BUTTON;
	}

	@Override
	public void withDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@NotNull
	@Override
	public Button withEmoji(@NotNull PartialEmoji emoji) {
		this.emoji = emoji;
		return this;
	}

	@Override
	@Nullable
	public PartialEmoji getEmoji() {
		return this.emoji;
	}

	public enum Style {
		PRIMARY(1),
		SECONDARY(2),
		SUCCESS(3),
		DANGER(4),
		LINK(5);

		Style(int value) {
			this.value = value;
		}

		private final int value;

		/**
		 * @return Integer used by discord to determine the button style
		 */
		public int getValue() {
			return value;
		}
	}
}