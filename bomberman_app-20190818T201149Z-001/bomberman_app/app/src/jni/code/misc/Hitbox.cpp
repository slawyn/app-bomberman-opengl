//
// Created by slaw on 5/10/2020.
//

#include "Hitbox.h"   // Generated


extern "C"
{
    void vHitboxUpdateEdges(Hitbox_t *pxHitbox1,
                            int16_t i16x,
                            int16_t i16y)
    {
        /*
        pxHitbox1->i16Left = (i16x + pxHitbox1->i16OffsetX);
        pxHitbox1->i16Right = (pxHitbox1->i16Left + pxHitbox1->i16HalfSizeX);
        pxHitbox1->i16Top = (i16y + pxHitbox1->i16OffsetY);
        pxHitbox1->i16Bottom = (pxHitbox1->i16Top + pxHitbox1->i16HalfSizeY);
         */
        pxHitbox1->i16Left = (i16x - pxHitbox1->i16HalfSizeX);
        pxHitbox1->i16Right = (i16x + pxHitbox1->i16HalfSizeX);
        pxHitbox1->i16Top = (i16y - pxHitbox1->i16HalfSizeY);
        pxHitbox1->i16Bottom = (i16y + pxHitbox1->i16HalfSizeY);
    }

    bool bHitboxIntersects(Hitbox_t *pxHitboxThis,
                           Hitbox_t *pxHitbox2)
    {
        return (pxHitboxThis->i16Left < pxHitbox2->i16Right
                && pxHitboxThis->i16Right > pxHitbox2->i16Left
                && pxHitboxThis->i16Top < pxHitbox2->i16Bottom
                && pxHitboxThis->i16Bottom > pxHitbox2->i16Top);
    }

    Hitbox_t* pxcollisionCheck(Hitbox_t* pxHitboxThis, Hitbox_t* pxHitbox2)
    {
        Hitbox_t * pxHitboxCurrent = pxHitbox2;
        Hitbox_t * pxHitboxCollidedWith = NULL;
        if(NULL != pxHitboxThis && NULL != pxHitboxCurrent)
        {
            while(pxHitboxCurrent)
            {
                if(bHitboxIntersects(pxHitboxThis, pxHitboxCurrent))
                {
                    pxHitboxCollidedWith = pxHitboxCurrent;
                    break;
                }
                else
                {
                    pxHitboxCurrent = pxHitboxCurrent->pxHitboxNext;
                }
            }
        }

        return pxHitboxCollidedWith;
    }
}