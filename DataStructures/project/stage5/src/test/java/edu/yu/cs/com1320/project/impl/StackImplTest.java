package edu.yu.cs.com1320.project.impl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StackImplTest {
    @Test
    void stackTest1(){
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(1);
        assertEquals(1, stack.peek());
        stack.push(2);
        assertEquals(2, stack.peek());
        stack.push(3);
        assertEquals(3, stack.peek());
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertNull(stack.pop());
    }

    @Test
    void stackOfStacks(){
        StackImpl<StackImpl<Integer>> stack = new StackImpl<>();
        StackImpl<Integer> innerStack = new StackImpl<Integer>();
        StackImpl<Integer> innerStack2 = new StackImpl<Integer>();
        StackImpl<Integer> innerStack3 = new StackImpl<Integer>();
        innerStack.push(1);
        innerStack.push(2);
        innerStack.push(3);
        innerStack2.push(10);
        innerStack2.push(20);
        innerStack2.push(30);
        innerStack3.push(100);
        innerStack3.push(200);
        innerStack3.push(300);
        stack.push(innerStack);
        stack.push(innerStack2);
        stack.push(innerStack3);
        assertEquals(300, stack.pop().pop());
        assertEquals(30, stack.pop().peek());
        assertEquals(3, stack.peek().pop());
    }
}
