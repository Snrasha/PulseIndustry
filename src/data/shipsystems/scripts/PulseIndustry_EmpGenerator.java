package src.data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import src.data.scripts.utils.PulseIndustry_Timer;
import src.data.scripts.utils.PulseIndustry_weaponListPosition;

public class PulseIndustry_EmpGenerator extends BaseShipSystemScript {

    private static final float EMPDAMAGE = 100;
    private static final float DAMAGE = 30;

    private  PulseIndustry_Timer emptimer = null;
    private static final Map RANGE = new HashMap();

    static {
        RANGE.put(ShipAPI.HullSize.FRIGATE, 400f);
        RANGE.put(ShipAPI.HullSize.DESTROYER, 600f);
        RANGE.put(ShipAPI.HullSize.CRUISER, 700f);
        RANGE.put(ShipAPI.HullSize.CAPITAL_SHIP, 900f);
    }

    private static final Color INNERCOLOR = new Color(200, 75, 200, 200);
    private static final Color OUTERCOLOR = new Color(255, 100, 255, 255);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {

        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (state == State.ACTIVE) {
            if (emptimer == null) {
                emptimer = new PulseIndustry_Timer(20, PulseIndustry_weaponListPosition.posWeapon(ship));
            }
            emptimer.advance();
            if (emptimer.isElapsed()) {

                CombatEntityAPI[] targets = new CombatEntityAPI[2];
                float range = (Float) RANGE.get(ship.getHullSize());

                Iterator<MissileAPI> itermissile = AIUtils.getNearbyEnemyMissiles(ship, range).iterator();
                MissileAPI missiletoTarget;
                // focus one time every missile on the range.
                int i = 0;
                while (itermissile.hasNext()) {
                    missiletoTarget = itermissile.next();
                    if (missiletoTarget != null && missiletoTarget.isArmed()) {
                        targets[i] = missiletoTarget;
                        i++;
                        if (i == targets.length) {
                            break;
                        }
                    }
                }

                if (i < targets.length) {
                    List<ShipAPI> listship = AIUtils.getNearbyEnemies(ship, range);
                    Iterator<ShipAPI> itership = listship.iterator();
                    ShipAPI shiptoTarget;

                    // If have always space, focus one time every missile on the range.
                    while (itership.hasNext()) {
                        shiptoTarget = itership.next();
                        if (shiptoTarget != null) {
                            if (shiptoTarget.isFighter()) {
                                targets[i] = shiptoTarget;
                                i++;
                                if (i == targets.length) {
                                    break;
                                }
                            }
                        }
                    }
                }

                Vector2f[] positionW = emptimer.getPosition();

                i = 0;
                for (CombatEntityAPI target : targets) {
                    Vector2f loc = ship.getLocation();
                    if (positionW != null) {
                        if (i >= positionW.length) {
                            i = 0;
                        }

                        Vector2f vect = positionW[i];
                        if (Math.abs(vect.getX()) < Math.abs(vect.getY())) {
                            vect = new Vector2f(vect.getY(), vect.getX());
                        }
                        VectorUtils.rotate(vect, ship.getFacing(), vect);
                        loc = new Vector2f(loc.getX() + vect.getX(), loc.getY() + vect.getY());
                    }

                    i++;

                    if (target != null) {
                        Global.getCombatEngine().spawnEmpArc(ship, loc, ship, target,
                                DamageType.ENERGY,
                                DAMAGE,
                                EMPDAMAGE, // emp 
                                100000f, // max range 
                                "system_emp_emitter_impact",
                                8, // thickness
                                OUTERCOLOR, INNERCOLOR
                        );
                    } else {
                        Global.getCombatEngine().spawnEmpArc(ship, loc, ship, ship,
                                DamageType.ENERGY,
                                0,
                                0, // emp 
                                100000f, // max range 
                                "system_emp_emitter_impact",
                                8, // thickness
                                OUTERCOLOR, INNERCOLOR
                        );
                    }
                }

            }
        } 
    }

}
