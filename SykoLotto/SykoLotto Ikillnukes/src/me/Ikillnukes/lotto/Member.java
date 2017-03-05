package me.Ikillnukes.lotto;

public class Member {
	public enum ModifyField { amount, autoenter, nextroundamount }
	public int amount;
	public boolean autoenter;
	public int nextRoundAmount;
	public Member(int a, boolean ae, int nra) {
		amount = a;
		autoenter = ae;
		nextRoundAmount = nra;
	}
	public Member modify(ModifyField action, Object newvalue) {
		if(action == ModifyField.amount) {
			amount = (int)newvalue;
		} else if(action == ModifyField.autoenter) {
			autoenter = (boolean)newvalue;
		} else if(action == ModifyField.nextroundamount) {
			nextRoundAmount = (int)newvalue;
		}
		return this;
	}
}
