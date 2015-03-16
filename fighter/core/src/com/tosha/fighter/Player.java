/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tosha.fighter;

import com.badlogic.gdx.physics.box2d.*;

/**
 *
 * @author Aantokhin
 */
public class Player {
    private float maxVelocityX  = 1000.0f;
    private float maxVelocityY  = 2000.0f;
    
    private float speedX  = 1000.0f;
    private float speedY  = 2000.0f;
    
    private float breakSpeedX  = 2000.0f;
    private float breakSpeedY  = 2000.0f;
    
    private boolean onGround;
    
    private Body body;
    private World world;
    
    private boolean headingLeft = true;

    public Player(World world) {
        this.world = world; //reference to game world
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(100, 100);
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(30, 30);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.2f;
        //fixtureDef.friction = 0.4f;
        //fixtureDef.restitution = 0.0f; // Make it bounce a little bit
        //fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        
        body.setFixedRotation(true);
        body.setLinearDamping(-0.2f);
        body.setUserData(new BodyData("player", 100, 100));
        
    }

    public boolean isHeadingLeft() {
        return headingLeft;
    }

    public void setHeadingLeft(boolean headingLeft) {
        this.headingLeft = headingLeft;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
    
    public float getMaxVelocityX() {
        return maxVelocityX;
    }

    public void setMaxVelocityX(float maxVelocityX) {
        this.maxVelocityX = maxVelocityX;
    }

    public float getMaxVelocityY() {
        return maxVelocityY;
    }

    public void setMaxVelocityY(float maxVelocityY) {
        this.maxVelocityY = maxVelocityY;
    }

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
        this.speedY = speedY;
    }

    public float getBreakSpeedX() {
        return breakSpeedX;
    }

    public void setBreakSpeedX(float breakSpeedX) {
        this.breakSpeedX = breakSpeedX;
    }

    public float getBreakSpeedY() {
        return breakSpeedY;
    }

    public void setBreakSpeedY(float breakSpeedY) {
        this.breakSpeedY = breakSpeedY;
    }


    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
    
    
}
