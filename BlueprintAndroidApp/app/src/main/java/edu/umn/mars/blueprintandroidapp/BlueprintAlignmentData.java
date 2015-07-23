package edu.umn.mars.blueprintandroidapp;

/**
 * Created by mars on 7/23/15.
 */
public class BlueprintAlignmentData {
    static final float InitialTrajPosX = 0;
    static final float InitialTrajPosY = 0;
    static final float InitialTrajRot = 0;
    static final float InitialTrajScale = 100.0f;

    public float TrajPosX;
    public float TrajPosY;
    public float TrajRot;
    public float TrajScale;

    BlueprintAlignmentData() {
        ResetAlignmentData();
    }

    public void ResetAlignmentData() {
        TrajScale = InitialTrajScale;
        TrajPosX = InitialTrajPosX;
        TrajPosY = InitialTrajPosY;
        TrajRot = InitialTrajRot;
    }
}
