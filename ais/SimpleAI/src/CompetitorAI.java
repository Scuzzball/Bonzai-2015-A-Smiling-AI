import snowbound.api.*;
import snowbound.api.util.*;
import static snowbound.api.util.Utility.*;

import java.util.*;
 
@Agent(name = "SimpleAI")
public class CompetitorAI extends AI {
	
	private Map<Unit, Base> goals;
	
	public CompetitorAI() {
		this.goals = new HashMap<>();
	}
	
	public Action action(Turn turn) {
		Unit actor = turn.actor();
	
		// spawn if you need to (who cares if there is someone already there)
		if(actor.position() == null) { 
			return new SpawnAction(any(turn.actor().team().spawns()), Perk.NONE);
		}
		
		// if we have a goal and its satisfied, remove it
		if(goals.containsKey(actor) && actor.team().equals(turn.current(goals.get(actor)).owner())) {
			goals.remove(actor);
		}
		
		// if we don't have a goal yet, choose one
		if(!goals.containsKey(actor)) {
			Base choice = any(difference(turn.allBases(), turn.myBases()));
			if(choice != null) { goals.put(actor, choice); }
		}
		
		if(actor.snowballs() < actor.statistic(Stat.CAPACITY) && Math.random() < 0.25) {
			System.out.println("Gathering Snow! (have " + actor.snowballs() + " now)");
			return new GatherAction();
		}
		
		// respond reactively, if needed
		Unit tango = any(actor.enemyUnitsInThrowRange());
		if(tango != null) {
			System.out.printf("Throwing at %s with %d snowballs!%n", tango, actor.snowballs());
			return new ThrowAction(tango.position());
		}
		
		// act out that goal
		Base goal = goals.get(actor);
		if(goal != null) {
			if(goal.position().equals(actor.position())) { return new CaptureAction(); }
		
			Position target = min(actor.positionsInMoveRange(), new ManhattanDistance(goal));
			return new MoveAction(target);
		}
		
		return null;
	}
}