package com.env.bean;

public class EnumList {

	public enum AppRightState{
		
		ClientApplication(0), ServerAuditing(1), AuditPass(2), AuditNotPass(3);

		private int mState;

		private AppRightState(int state) {
			mState = state;
		}

		public static AppRightState getApplicationState(int state) {
			switch (state) {
			case 0:
				return AppRightState.ClientApplication;
			case 1:
				return AppRightState.ServerAuditing;
			case 2:
				return AppRightState.AuditPass;
			case 3:
				return AppRightState.AuditNotPass;
			}
			return null;
		}

		public int getStae() {
			return mState;
		}
		
	}
	
	public enum AccountState{
		StateTrue(1),StateFalse(0);
		
		private int state;
		
		private AccountState (int state){
			this.state = state;
		}
		
		public static AccountState getAccountState(int state){
			switch (state) {
			case 0:
				return StateFalse;

			case 1:
				return StateTrue;
			}
			return null;
		}
		
		public int getState(){
			return state;
		}
		
	}
	
	

}
