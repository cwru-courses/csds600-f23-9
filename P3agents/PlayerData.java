package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.environment.model.state.*;

public class PlayerData {
	public int id, playerHp, xPosition, yPosition, playerDamage;

	public PlayerData(Unit.UnitView unitView) {
		this.playerHp = unitView.getHP();
		this.xPosition = unitView.getXPosition();
		this.yPosition = unitView.getYPosition();
		this.playerDamage = unitView.getTemplateView().getBasicAttack();
		id=unitView.getID();
	}


	public PlayerData(PlayerData data){
        this.id = data.id;
		this.xPosition = data.xPosition;
		this.yPosition = data.yPosition;
		this.playerHp = data.playerHp;
		this.playerDamage = data.playerDamage;
	}

}
