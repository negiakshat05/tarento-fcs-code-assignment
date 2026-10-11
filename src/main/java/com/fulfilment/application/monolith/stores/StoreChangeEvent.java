package com.fulfilment.application.monolith.stores;

public final class StoreChangeEvent {

	public enum Type {
		CREATED, UPDATED
	}

	private final Type type;
	private final Store store;

	private StoreChangeEvent(Type type, Store store) {
		this.type = type;
		this.store = snapshot(store);
	}

	public static StoreChangeEvent created(Store store) {
		return new StoreChangeEvent(Type.CREATED, store);
	}

	public static StoreChangeEvent updated(Store store) {
		return new StoreChangeEvent(Type.UPDATED, store);
	}

	public Type getType() {
		return type;
	}

	public Store getStore() {
		return snapshot(store);
	}

	private static Store snapshot(Store source) {
		Store copy = new Store();
		copy.id = source.id;
		copy.name = source.name;
		copy.quantityProductsInStock = source.quantityProductsInStock;
		return copy;
	}
}
