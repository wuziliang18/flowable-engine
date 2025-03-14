/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flowable.common.engine.impl.context;

import java.util.Stack;

import org.flowable.common.engine.impl.cfg.TransactionContext;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.transaction.TransactionContextHolder;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Joram Barrez
 * 管理CommandContext和TransactionContext
 */
public class Context {
    //一个线程中可能会执行多次命令 一个命令内也可以去执行其他命令所以用栈
    protected static ThreadLocal<Stack<CommandContext>> commandContextThreadLocal = new ThreadLocal<>();

    public static CommandContext getCommandContext() {
        Stack<CommandContext> stack = getStack(commandContextThreadLocal);
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }
    /**
     * 每次使用的时候要push进去 如果共享的且在一个命令中调用另一个命令 会出现 a->a->b的情况 b是出现异常的情况 
     * @param commandContext
     */
    public static void setCommandContext(CommandContext commandContext) {
        getStack(commandContextThreadLocal).push(commandContext);
    }
    /**
     * 使用完后pop 
     */
    public static void removeCommandContext() {
        getStack(commandContextThreadLocal).pop();
    }

    public static TransactionContext getTransactionContext() {
        return TransactionContextHolder.getTransactionContext();
    }

    public static void setTransactionContext(TransactionContext transactionContext) {
        TransactionContextHolder.setTransactionContext(transactionContext);
    }

    public static void removeTransactionContext() {
        TransactionContextHolder.removeTransactionContext();
    }

    protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
        Stack<T> stack = threadLocal.get();
        if (stack == null) {
            stack = new Stack<>();
            threadLocal.set(stack);
        }
        return stack;
    }

}
