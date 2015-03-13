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
    private float maxVelocityY  = 1000.0f;
    private float maxVelocityAngular = 2000.0f;
    
    private float speedX  = 100.0f;
    private float speedY  = 100.0f;
    private float speedAngular = 500.0f;
    
    private float breakSpeedX  = 200.0f;
    private float breakSpeedY  = 200.0f;
    private float breakSpeedAngular = 1500.0f;
    
    private Body body;
    private World world;

    public Player(World world) {
        this.world = world; //reference to game world
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(100, 300);
        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(12f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        Fixture fixture = body.createFixture(fixtureDef);
        
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

    public float getMaxVelocityAngular() {
        return maxVelocityAngular;
    }

    public void setMaxVelocityAngular(float maxVelocityAngular) {
        this.maxVelocityAngular = maxVelocityAngular;
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

    public float getSpeedAngular() {
        return speedAngular;
    }

    public void setSpeedAngular(float speedAngular) {
        this.speedAngular = speedAngular;
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

    public float getBreakSpeedAngular() {
        return breakSpeedAngular;
    }

    public void setBreakSpeedAngular(float breakSpeedAngular) {
        this.breakSpeedAngular = breakSpeedAngular;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
    
    
}
