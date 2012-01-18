/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.reporting.repository.global;

import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.remote.DefaultRemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>DefaultGlobalReportRepository class.</p>
 * <p/>
 * Class realize the global report repository. It provides a local repository for community reports and reads one or
 * more configurations for remote repositories.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.8.1
 */
@ContextConfiguration(locations = {
        "classpath:META-INF/opennms/applicationContext-reportingRepository.xml",
        "classpath:META-INF/opennms/applicationContext-reportingDao.xml"})
public class DefaultGlobalReportRepository implements GlobalReportRepository {

    /**
     * Logging
     */
    private final Logger logger = LoggerFactory.getLogger(DefaultGlobalReportRepository.class);

    /**
     * Configuration DAO to build remote repositories from  remote-reports.xml
     */
    private RemoteRepositoryConfigDao m_remoteRepositoryConfigDao;

    /**
     * The local report repository with all OpenNMS community reports
     */
    private ReportRepository m_localReportRepository;

    /**
     * The remote report repository with OpenNMS CONNECT reports
     */
    private ReportRepository m_remoteReportRepository;

    /**
     * Concatenated repositoryId and reportId by "_"
     */
    private final String REPOSITORY_REPORT_SEP = "_";

    /**
     * List for repositories, one local disk and all configured remote repositories
     */
    private final List<ReportRepository> m_repositoryList;

    /**
     * JasperReports version number
     */
    private String m_jasperReportVersion;


    /**
     * Default constructor creates one local and many remote repositories.
     */
    public DefaultGlobalReportRepository(RemoteRepositoryConfigDao remoteRepositoryConfigDao, ReportRepository reportRepository) {
        m_remoteRepositoryConfigDao = remoteRepositoryConfigDao;
        m_localReportRepository = reportRepository;

        // Get the jasper report version from opennms.properties
        m_jasperReportVersion = System.getProperty("org.opennms.jasperReportsVersion");

        this.m_repositoryList = new ArrayList<ReportRepository>();

        try {
            logger.debug("Config resource is set to '{}'", m_remoteRepositoryConfigDao.toString());
            Assert.notNull(m_remoteRepositoryConfigDao, "remote repository config dao property configResource must be set to a non-null value");

            logger.debug("Local report repository is set to '{}'", m_localReportRepository.toString());
            Assert.notNull(m_localReportRepository, "local report repository property must be set to a non-null value");
        } catch (Exception e) {
            logger.error("Error during create a default global report repository. Error message: '{}'", e.getMessage());
        }

        try {
            logger.debug("JasperReports version is set to '{}'", m_jasperReportVersion);
            Assert.notNull(m_jasperReportVersion, "jasper report version must be set to a non-null value");
        } catch (Exception e) {
            logger.error("Jasper report version must be set in opennms.properties. Error message: '{}'", e.getMessage());
        }
        setRemoteRepositoryConfigDao(remoteRepositoryConfigDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getAllReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repository : m_repositoryList) {
            results.addAll(repository.getReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getReports(String repositoryId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repository = this.getRepositoryById(repositoryId);
        if (repository != null) {
            results.addAll(repository.getReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getAllOnlineReports() {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        for (ReportRepository repository : m_repositoryList) {
            results.addAll(repository.getOnlineReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getOnlineReports(String repositoryId) {
        List<BasicReportDefinition> results = new ArrayList<BasicReportDefinition>();
        ReportRepository repository = this.getRepositoryById(repositoryId);
        if (repository != null) {
            results.addAll(repository.getOnlineReports());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReportService(String reportId) {
        String result = "";
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            result = repository.getReportService(reportId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName(String reportId) {
        String result = "";
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            result = repository.getDisplayName(reportId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEngine(String reportId) {
        String result = "";
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            result = repository.getEngine(reportId);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getTemplateStream(String reportId) {
        InputStream templateStream = null;
        ReportRepository repository = this.getRepositoryForReport(reportId);
        if (repository != null) {
            templateStream = repository.getTemplateStream(reportId);
        }
        return templateStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReportRepository> getRepositoryList() {
        return m_repositoryList;
    }

    /**
     * Add a report repository.
     *
     * @param repository a ReportRepository
     */
    @Override
    public void addReportRepository(ReportRepository repository) {
        m_repositoryList.add(repository);
    }

    /**
     * Get a specific repository identified by repository id.
     *
     * @param repositoryId a String as repository identifier
     * @return report repository as {@link org.opennms.features.reporting.repository.ReportRepository} object
     */
    @Override
    public ReportRepository getRepositoryById(String repositoryId) {
        for (ReportRepository repository : m_repositoryList) {
            if (repositoryId.equals(repository.getRepositoryId())) {
                // leave if we have a repository
                return repository;
            }
        }
        logger.debug("No repository with id '{}' was found, return null", repositoryId);
        // we haven't a repository with repositoryId
        return null;
    }

    /**
     * Get a specific repository identified by a report id.
     *
     * @param reportId a String as report identifier
     * @return report repository as {@link org.opennms.features.reporting.repository.ReportRepository} object
     */
    protected ReportRepository getRepositoryForReport(String reportId) {
        String repositoryId = reportId.substring(0, reportId.indexOf(REPOSITORY_REPORT_SEP));
        return this.getRepositoryById(repositoryId);
    }

    /**
     * <p>setLocalReportRepository</p>
     * <p/>
     * Set the legacy local repository which provides all OpenNMS community reports
     *
     * @param reportRepository a {@link org.opennms.features.reporting.repository.local.LegacyLocalReportRepository} object
     */
    public void setLocalReportRepository(ReportRepository reportRepository) {
        m_localReportRepository = reportRepository;
    }

    /**
     * <p>getReportRepository</p>
     * <p/>
     * Get the legacy local repository which provides all OpenNMS community reports
     *
     * @return a {@link org.opennms.features.reporting.repository.local.LegacyLocalReportRepository} object
     */
    public ReportRepository getReportRepository() {
        return m_localReportRepository;
    }

    /**
     * <p>setRemoteReportRepository</p>
     * <p/>
     * Set the remote repository for OpenNMS CONNECT reports
     *
     * @param remoteReportRepository a {@link org.opennms.features.reporting.repository.remote.DefaultRemoteRepository} object
     */
    public void setRemoteReportRepository(ReportRepository remoteReportRepository) {
        m_remoteReportRepository = remoteReportRepository;
    }

    /**
     * <p>getRemoteReportRepository</p>
     * 
     * @return a {@link org.opennms.features.reporting.repository.remote.DefaultRemoteRepository} object
     */
    public ReportRepository getRemoteReportRepository() {
        return m_remoteReportRepository;
    }

    /**
     * <p>setRemoteRepositoryConfigDao</p>
     * <p/>
     * Set the default remote report repository which provides access to OpenNMS CONNECT reports
     *
     * @param remoteRepositoryConfigDao a {@link org.opennms.features.reporting.repository.remote.DefaultRemoteRepository} object
     */
    public void setRemoteRepositoryConfigDao(RemoteRepositoryConfigDao remoteRepositoryConfigDao) {
        m_remoteRepositoryConfigDao = remoteRepositoryConfigDao;

        /**
         * The local disk repository provides the canned OpenNMS community reports.
         */
        this.m_repositoryList.add(m_localReportRepository);

        /**
         * Create a list with all remote repositories from remote repository for each remote repository from RemoteRepositoryConfig.
         */
        //TODO tak: This is tricky to test and to mock, we have to refactor this
        try {
            for (RemoteRepositoryDefinition repositoryDefinition : m_remoteRepositoryConfigDao.getActiveRepositories()) {
                this.m_repositoryList.add(new DefaultRemoteRepository(repositoryDefinition, m_jasperReportVersion));
            }
        } catch (Exception e) {
            logger.error("Could not add configured remote repositories in default global report repository. Error message: '{}'", e.getMessage());
        }
    }

    /**
     * <p>getRemoteRepositoryConfigDao</p>
     * <p/>
     * Get the default remote report repository which provides access to OpenNMS CONNECT reports
     *
     * @return a {@link org.opennms.features.reporting.repository.remote.DefaultRemoteRepository} object
     */
    public RemoteRepositoryConfigDao getRemoteRepositoryConfigDao() {
        return m_remoteRepositoryConfigDao;
    }
}