package me.SgtMjrME.ClassUpdate.Abilities;

class cooldown {
	BaseAbility a;
	Long time;

	cooldown(BaseAbility ba) {
		a = ba;
		time = Long.valueOf(System.currentTimeMillis());
	}
}