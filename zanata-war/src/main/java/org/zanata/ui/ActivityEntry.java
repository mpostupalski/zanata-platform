/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.ui;

import org.apache.commons.lang.StringEscapeUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ActivityType;
import org.zanata.common.EntityStatus;
import org.zanata.dao.DocumentDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.Activity;
import org.zanata.model.HDocument;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.EntityType;
import org.zanata.service.ActivityService;
import org.zanata.util.DateUtil;
import org.zanata.util.ShortString;
import org.zanata.util.UrlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static org.zanata.common.ActivityType.REVIEWED_TRANSLATION;
import static org.zanata.common.ActivityType.UPDATE_TRANSLATION;
import static org.zanata.common.ActivityType.UPLOAD_SOURCE_DOCUMENT;
import static org.zanata.common.ActivityType.UPLOAD_TRANSLATION_DOCUMENT;

/**
 * Provides data and operations needed to display an activity entry.
 *
 * This is used by template activity-entry.xhtml
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("activityEntry")
@Scope(ScopeType.STATELESS)
@AutoCreate
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityEntry {
    @In
    private ActivityService activityServiceImpl;

    @In
    private UrlUtil urlUtil;

    @In
    private DocumentDAO documentDAO;

    @In
    private Messages msgs;

    public String getActivityTypeIconClass(Activity activity) {
        return activity.getActivityType() == UPDATE_TRANSLATION ? "i--translate" :
            activity.getActivityType() == REVIEWED_TRANSLATION ? "i--review" :
                activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT ? "i--document" :
                    activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT ? "i--translate-up" :
                        "";
    }

    public String getActivityTitle(Activity activity) {
        return activity.getActivityType() == UPDATE_TRANSLATION ?
            msgs.get("jsf.Translation") :
            activity.getActivityType() == REVIEWED_TRANSLATION ?
                msgs.get("jsf.Reviewed") :
                activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT ?
                    msgs.get("jsf.UploadedSource") :
                    activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT ?
                        msgs.get("jsf.UploadedTranslations") :
                        "";
    }

    public String getActivityMessage(Activity activity) {
        boolean isVersionDeleted = isVersionDeleted(activity);
        boolean isProjectDeleted = isProjectDeleted(activity);

        switch (activity.getActivityType()) {
            case UPDATE_TRANSLATION:
            if (isProjectDeleted) {
                return msgs
                        .format(
                            "jsf.dashboard.activity.translate.message.projectDeleted",
                            activity.getWordCount(),
                            getProjectName(activity),
                            StringEscapeUtils
                                .escapeHtml(getLastTextFlowContent(activity)));
            } else if (isVersionDeleted) {
                return msgs
                        .format("jsf.dashboard.activity.translate.message.versionDeleted",
                                activity.getWordCount(),
                                getProjectUrl(activity),
                                getProjectName(activity),
                                StringEscapeUtils
                                        .escapeHtml(
                                        getLastTextFlowContent(activity)));
            } else {
                return msgs
                        .format("jsf.dashboard.activity.translate.message",
                                activity.getWordCount(),
                                getProjectUrl(activity),
                                getProjectName(activity),
                                getEditorUrl(activity),
                                StringEscapeUtils
                                        .escapeHtml(
                                        getLastTextFlowContent(activity)));
            }
            case REVIEWED_TRANSLATION:
                if(isProjectDeleted) {
                    return msgs.format("jsf.dashboard.activity.review.message.projectDeleted",
                        activity.getWordCount(), getProjectName(activity),
                        StringEscapeUtils
                            .escapeHtml(getLastTextFlowContent(activity)));
                } else if(isVersionDeleted) {
                    return msgs.format("jsf.dashboard.activity.review.message.versionDeleted",
                        activity.getWordCount(), getProjectUrl(activity),
                        getProjectName(activity),
                        StringEscapeUtils
                            .escapeHtml(getLastTextFlowContent(activity)));
                } else {
                    return msgs.format("jsf.dashboard.activity.review.message",
                        activity.getWordCount(), getProjectUrl(activity),
                        getProjectName(activity), getEditorUrl(activity),
                        StringEscapeUtils
                            .escapeHtml(getLastTextFlowContent(activity)));
                }
            case UPLOAD_SOURCE_DOCUMENT:
                if(isProjectDeleted) {
                    return msgs
                        .format("jsf.dashboard.activity.uploadSource.message.projectDeleted",
                            activity.getWordCount(), getProjectName(activity));
                } else {
                    return msgs
                        .format("jsf.dashboard.activity.uploadSource.message",
                            activity.getWordCount(), getProjectUrl(activity),
                            getProjectName(activity));
                }
            case UPLOAD_TRANSLATION_DOCUMENT:
                if(isProjectDeleted) {
                    return msgs.format(
                        "jsf.dashboard.activity.uploadTranslation.message.projectDeleted",
                        activity.getWordCount(), getProjectName(activity));
                } else {
                    return msgs.format(
                        "jsf.dashboard.activity.uploadTranslation.message",
                        activity.getWordCount(), getProjectUrl(activity),
                        getProjectName(activity));
                }
            default:
                return "";
        }
    }

    public String getWordsCountMessage(int wordCount) {
        if (wordCount == 1) {
            return wordCount + " word";
        }
        return wordCount + " words";
    }

    public String getProjectUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());

        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            return urlUtil.projectUrl(version.getProject().getSlug());
        }
        return "";
    }

    public String getEditorUrl(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HProjectIteration version = (HProjectIteration) context;
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

            if (tft != null) {
                return urlUtil
                    .editorTransUnitUrl(version.getProject().getSlug(),
                        version.getSlug(), tft.getLocaleId(), tft.getTextFlow()
                            .getLocale(), tft.getTextFlow().getDocument()
                            .getDocId(), tft.getTextFlow().getId());
            }
        } else if (activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT) {
            // not supported for upload source action
        } else if (activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                    documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                return urlUtil.editorTransUnitUrl(version.getProject()
                        .getSlug(), version.getSlug(), tft.getLocaleId(),
                        document.getSourceLocaleId(), tft.getTextFlow()
                                .getDocument().getDocId(), tft.getTextFlow()
                                .getId());
            }
        }
        return "";
    }

    public String getDocumentUrl(Activity activity) {
        String url = "";
        Object context =
            getEntity(activity.getContextType(), activity.getContextId());
        Object lastTarget =
            getEntity(activity.getLastTargetType(),
                activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HProjectIteration version = (HProjectIteration) context;
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

            url =
                urlUtil.editorDocumentUrl(version.getProject().getSlug(),
                    version.getSlug(), tft.getLocaleId(), tft
                        .getTextFlow().getLocale(), tft
                        .getTextFlow().getDocument().getDocId());
        } else if (activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            url =
                urlUtil.sourceFilesViewUrl(version.getProject().getSlug(),
                    version.getSlug());
        } else if (activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                url =
                    urlUtil.editorDocumentUrl(version.getProject()
                            .getSlug(), version.getSlug(), tft
                            .getLocaleId(), document.getSourceLocaleId(),
                        tft.getTextFlow().getDocument().getDocId());
            }
        }
        return url;
    }

    public String getVersionUrl(Activity activity) {
        Object context =
            getEntity(activity.getContextType(), activity.getContextId());
        String url = "";

        if (isTranslationUpdateActivity(activity.getActivityType())
            || activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT
            || activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            url =
                urlUtil.versionUrl(version.getProject().getSlug(),
                    version.getSlug());
        }

        return url;
    }

    public String getDocumentListUrl(Activity activity) {
        Object context =
            getEntity(activity.getContextType(), activity.getContextId());
        Object lastTarget =
            getEntity(activity.getLastTargetType(),
                activity.getLastTargetId());
        String url = "";

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HProjectIteration version = (HProjectIteration) context;
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;

            url =
                urlUtil.editorDocumentListUrl(version.getProject()
                        .getSlug(), version.getSlug(), tft.getLocaleId(),
                    tft.getTextFlow().getLocale());
        } else if (activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT) {
            // not supported for upload source action
        } else if (activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                url =
                    urlUtil.editorDocumentListUrl(version.getProject()
                        .getSlug(), version.getSlug(), tft
                        .getLocaleId(), tft.getTextFlow().getLocale());
            }
        }
        return url;
    }

    public String getProjectName(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());

        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            HProjectIteration version = (HProjectIteration) context;
            return version.getProject().getName();
        }
        return "";
    }

    public String getVersionName(Activity activity) {
        HProjectIteration version = getVersion(activity);
        if (version == null) {
            return "";
        } else {
            return version.getSlug();
        }
    }

    public HProjectIteration getVersion(Activity activity) {
        Object context =
                getEntity(activity.getContextType(), activity.getContextId());
        if (isTranslationUpdateActivity(activity.getActivityType())
                || activity.getActivityType() == ActivityType.UPLOAD_SOURCE_DOCUMENT
                || activity.getActivityType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT) {
            return (HProjectIteration) context;
        }
        return null;
    }

    public String getDocumentName(Activity activity) {
        Object lastTarget =
            getEntity(activity.getLastTargetType(),
                activity.getLastTargetId());
        String docName = "";

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
            HTextFlow tf = tft.getTextFlow();
            if(tf != null) {
                docName = tf.getDocument().getName();
            }
        } else if (activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT
            || activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT) {
            HDocument document = (HDocument) lastTarget;
            docName = document.getName();
        }
        return docName;
    }

    public String getLanguageName(Activity activity) {
        Object lastTarget =
            getEntity(activity.getLastTargetType(),
                activity.getLastTargetId());
        String name = "";

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
            if (tft.getLocale() != null) {
                name = tft.getLocaleId().getId();
            }
        } else if (activity.getActivityType() == UPLOAD_SOURCE_DOCUMENT) {
            // not supported for upload source action
        } else if (activity.getActivityType() == UPLOAD_TRANSLATION_DOCUMENT) {
            HDocument document = (HDocument) lastTarget;
            HTextFlowTarget tft =
                documentDAO.getLastTranslatedTargetOrNull(document.getId());

            if (tft != null) {
                name = tft.getLocaleId().getId();
            }
        }

        return name;
    }

    public String getLastTextFlowContent(Activity activity) {
        String content = "";
        Object lastTarget =
                getEntity(activity.getLastTargetType(),
                        activity.getLastTargetId());

        if (isTranslationUpdateActivity(activity.getActivityType())) {
            HTextFlowTarget tft = (HTextFlowTarget) lastTarget;
            HTextFlow tf = tft.getTextFlow();
            if(tf != null) {
                content = tf.getContents().get(0);
            }
        }

        return ShortString.shorten(content);
    }

    public String getHowLongAgoDescription(Activity activity) {
        return DateUtil.getHowLongAgoDescription(activity.getLastChanged());
    }

    private boolean isTranslationUpdateActivity(ActivityType activityType) {
        return activityType == ActivityType.UPDATE_TRANSLATION
                || activityType == ActivityType.REVIEWED_TRANSLATION;
    }

    private Object getEntity(EntityType contextType, long id) {
        return activityServiceImpl.getEntity(contextType, id);
    }

    public boolean isVersionDeleted(Activity activity) {
        Object context =
            getEntity(activity.getContextType(), activity.getContextId());

        HProjectIteration version = (HProjectIteration) context;
        return version.getStatus() == EntityStatus.OBSOLETE
            || version.getProject().getStatus() == EntityStatus.OBSOLETE;
    }

    public boolean isProjectDeleted(Activity activity) {
        Object context =
            getEntity(activity.getContextType(), activity.getContextId());

        HProjectIteration version = (HProjectIteration) context;
        return version.getProject().getStatus() == EntityStatus.OBSOLETE;

    }
}