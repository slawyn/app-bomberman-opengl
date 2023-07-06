//
// Created by Unixt on 6/22/2023.
//
#include "Explosion.h"
#include "../misc/States.h"
#include "../misc/Inputs.h"
#include "../misc/Hitbox.h"

extern "C"
{
static Hitbox_t xHitboxExplosion = {0};

int16_t i16ExplosionInit()
{
    /*
private void addExplosion( Bomb firstbomb)
{
    int mapWidth = mFieldMap[0].length;
    int mapHeight = mFieldMap.length;
    int strength;
    int owner;
    int explosionid;
    int cellposx;
    int cellposy;

    ConcurrentLinkedQueue<Bomb> triggeredBombs = new ConcurrentLinkedQueue<>();
    triggeredBombs.add(firstbomb);


    int[] state = mGameStateBuffer.getStateData(OBJ_BOMB);
    do
    {

        int explosionslot = mGameStateBuffer.getFreeSlot(OBJ_EXPLN);
        Bomb bomb = triggeredBombs.remove();
        state[bomb.mObjectStateOffset+2] = STATE_DEAD;
        cellposx = bomb.getCellFromCenteredX();
        cellposy = bomb.getCellFromCenteredY();
        strength = bomb.mRadius;
        owner = bomb.mOwner;

        explosionid = OBJ_EXPLN | explosionslot;

        mPlayers.get(owner).mBombCount++;

        int[] cellsCovered = {0, 0, 0, 0};
        boolean[] expansionUnderway = {true, true, true, true};
        mFieldMap[cellposx][cellposy] = (explosionid);

        for(int direction = 0; direction < expansionUnderway.length && expansionUnderway[direction]; direction++)
        {
            // Continue only if the exposion didn't hit a wall
            for(int radius = 1; radius <= strength; radius++)
            {
                int posx = 0;
                int posy = 0;

                switch(direction)
                {
                    case 0: // mLeft
                        posx = cellposx - radius;
                        posy = cellposy;
                        break;
                    case 1: // mRight
                        posx = cellposx + radius;
                        posy = cellposy;
                        break;
                    case 2: // mUp
                        posx = cellposx;
                        posy = cellposy - radius;
                        break;
                    case 3: // mDown
                        posx = cellposx;
                        posy = cellposy + radius;
                        break;
                }

                if(posx < 0 || posx == mapWidth || posy < 0 || posy == mapHeight)
                {
                    expansionUnderway[direction] = false;
                    continue;
                }

                int id = mFieldMap[posx][posy];
                if(id != OBJ_NONE)
                {
                    GameObject go = mAllObjects.get(id);
                    int type = go.mObjectType;
                    switch(type)
                    {
                        case OBJ_BOMB:
                            triggeredBombs.add((Bomb) go);
                            expansionUnderway[direction] = false;
                            break;
                        case OBJ_CRATE:
                            mGameStateBuffer.setState(OBJ_CRATE,go.mObjectStateOffset,STATE_DEAD);
                            expansionUnderway[direction] = false;
                            cellsCovered[direction]++;
                            break;
                        case OBJ_BLOCK:
                        case OBJ_EXPLN:
                            expansionUnderway[direction] = false;
                            break;
                    }
                } else
                {
                    mFieldMap[posx][posy] = (explosionid);
                    cellsCovered[direction]++;
                }
            }
        }


        mGameStateBuffer.setStateComplete(OBJ_EXPLN, explosionslot, getPositionXFromCell(cellposx), getPositionYFromCell(cellposy),STATE_ALIVE);
        Explosion explosion = new Explosion(explosionslot, cellposx, cellposy, owner, strength, cellsCovered);
        explosion.updateBoundingBoxes(mGameStateBuffer.getStateData(OBJ_EXPLN));
        int uniqueid = explosion.getUniqeueID();
        mExplosions.put(uniqueid, explosion);
        mAllObjects.put(uniqueid, explosion);

    } while(triggeredBombs.size() > 0);
}*/
    return 0;
}
    int16_t i16ExplosionUpdateState(Explosion_t *pxPlayer, int32_t dt) {


        return 1;
    }
}