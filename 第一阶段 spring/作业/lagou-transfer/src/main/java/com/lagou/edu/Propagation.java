package com.lagou.edu;

/**
 * @author Linzhi.Wang
 * @version V1.0
 * @Title: Propagation.java
 * @Package com.lagou.edu
 * @Description
 * @date 2020 04-13 15:52.
 */
public enum Propagation {

	REQUIRED(0),
	SUPPORTS(1),
	MANDATORY(2),
	REQUIRES_NEW(3),
	NOT_SUPPORTED(4),
	NEVER(5),
	NESTED(6);

	private final int value;

	private Propagation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}
}