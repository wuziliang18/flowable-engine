/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.engine.impl.interceptor;

import java.util.Map;

/**
 * @author Tom Baeyens
 * @author Joram 
 * CommandContext的生成工厂 
 */
public class CommandContextFactory {
    
    protected Map<Class<?>, SessionFactory> sessionFactories;

    public CommandContext createCommandContext(Command<?> cmd) {
        CommandContext commandContext = new CommandContext(cmd);
        //生成的时候会包装sessionFactories
        commandContext.setSessionFactories(sessionFactories);
        return commandContext;
    }

    public Map<Class<?>, SessionFactory> getSessionFactories() {
        return sessionFactories;
    }

    public void setSessionFactories(Map<Class<?>, SessionFactory> sessionFactories) {
        this.sessionFactories = sessionFactories;
    }
    
}