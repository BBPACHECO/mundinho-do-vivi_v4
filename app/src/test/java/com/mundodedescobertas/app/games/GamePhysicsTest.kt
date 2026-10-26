package com.mundodedescobertas.app.games

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GamePhysicsTest {
    @Test fun `flight waits for explicit input and flap raises the bird`() {
        val world=FlightWorld(0,Random(1))
        world.update(.02f,1f)
        assertEquals(.5f,world.y,0f)
        world.flap();repeat(5){world.update(.02f,1f)}
        assertTrue(world.y<.5f)
        assertFalse(world.collided)
    }
    @Test fun `resuming after a long gap does not advance a whole second`() {
        val a=FlightWorld(0,Random(2));val b=FlightWorld(0,Random(2))
        a.flap();b.flap();a.update(5f,1f);b.update(.04f,1f)
        assertEquals(a.y,b.y,.00001f)
        assertEquals(a.velocity,b.velocity,.00001f)
    }
    @Test fun `bird collisions use gap and score each passed obstacle once`() {
        val world=FlightWorld(0,Random(3));world.flap();world.y=.5f;world.velocity=0f
        world.obstacles.add(FlightObstacle(.21f,.05f))
        world.update(.001f,1f);assertTrue(world.collided)
        world.reset();world.flap();world.y=.5f;world.velocity=0f
        world.obstacles.add(FlightObstacle(.1f,.5f))
        world.update(.001f,1f);world.update(.001f,1f)
        assertEquals(1,world.score)
    }
    @Test fun `runner clamps lanes and jumping clears a low obstacle`() {
        val runner=RunnerWorld(Random(4))
        repeat(8){runner.move(1)};assertEquals(1,runner.lane)
        repeat(8){runner.move(-1)};assertEquals(-1,runner.lane)
        runner.objects.add(RunnerObject(.919f,-1,false));runner.leap();runner.jump=.2f
        runner.update(.02f,1f);assertEquals(0,runner.gentleBumps)
        runner.objects.add(RunnerObject(.919f,-1,true));runner.update(.02f,1f)
        assertEquals(1,runner.stars)
    }
    @Test fun `block world saves restores and rejects corrupted replacement`() {
        val world=BlockWorld();world.walk(2,-1);world.selected=0;world.place(2,3)
        val saved=world.snapshot();val restored=BlockWorld();restored.restore(saved)
        assertEquals(saved,restored.snapshot())
        restored.restore("VIVI1\n300,4\n2,3,0")
        assertEquals(saved,restored.snapshot())
        restored.restore("VIVI1\n2,3\n1,1,8")
        assertEquals(saved,restored.snapshot())
    }
    @Test fun `magnetic pieces snap only inside the accepted radius`() {
        val slots=listOf(100f to 100f,400f to 100f)
        assertEquals(0,snapSlot(130f,120f,slots,60f))
        assertEquals(1,snapSlot(390f,90f,slots,60f))
        assertNull(snapSlot(250f,500f,slots,60f))
    }
}
