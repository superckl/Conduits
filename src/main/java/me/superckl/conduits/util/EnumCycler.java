package me.superckl.conduits.util;

import lombok.Getter;

public class EnumCycler<T extends Enum<T>> {

	private final Class<T> enumClass;
	@Getter
	private T value;
	private int ordinal;

	public EnumCycler(final Class<T> enumClass, final T initial) {
		this.enumClass = enumClass;
		this.ordinal = initial.ordinal();
		this.value = enumClass.getEnumConstants()[this.ordinal];
	}

	public T cycle() {
		final T[] values = this.enumClass.getEnumConstants();
		this.ordinal = (this.ordinal + 1) % values.length;
		this.value = values[this.ordinal];
		return this.value;
	}

	public void setValue(final T value) {
		this.value = value;
		this.ordinal = value.ordinal();
	}

}
