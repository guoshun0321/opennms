/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 9, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.capsd;

import org.opennms.netmgt.model.discovery.IPAddress;

/**
 * <p>DefaultProcessorFactory class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DefaultProcessorFactory implements SuspectEventProcessorFactory, RescanProcessorFactory {
    
    private CapsdDbSyncer m_capsdDbSyncer;
    private PluginManager m_pluginManager;

    /**
     * <p>setCapsdDbSyncer</p>
     *
     * @param capsdDbSyncer a {@link org.opennms.netmgt.capsd.CapsdDbSyncer} object.
     */
    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

    /**
     * <p>setPluginManager</p>
     *
     * @param pluginManager a {@link org.opennms.netmgt.capsd.PluginManager} object.
     */
    public void setPluginManager(PluginManager pluginManager) {
        m_pluginManager = pluginManager;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.SuspectEventProcessorFactory#createSuspectEventProcessor(java.lang.String)
     */
    /** {@inheritDoc} */
    public SuspectEventProcessor createSuspectEventProcessor(final IPAddress ipAddress) {
        return new SuspectEventProcessor(m_capsdDbSyncer, m_pluginManager, ipAddress);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.RescanProcessorFactory#createRescanProcessor(int)
     */
    /** {@inheritDoc} */
    public RescanProcessor createRescanProcessor(int nodeId) {
        return new RescanProcessor(nodeId, false, m_capsdDbSyncer, m_pluginManager);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.RescanProcessorFactory#createForcedRescanProcessor(int)
     */
    /** {@inheritDoc} */
    public RescanProcessor createForcedRescanProcessor(int nodeId) {
        return new RescanProcessor(nodeId, true, m_capsdDbSyncer, m_pluginManager);
    }

}
