(ns kafka-connect-slack-sink.slack
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [aleph.http :as http]
            [cheshire.core :as json]
            [clostache.parser :refer [render]]
            [clojure.tools.logging :as log]
            [kafka-connect-slack-sink.config :as cfg]))

(defn render-template [template topic key value]
  (render template {:topic topic
                    :key (keywordize-keys key)
                    :value (keywordize-keys value)}))

(defn post-message [props topic key value]
  (let [webhook (get props cfg/SLACK_WEBHOOK)
        channel (get props cfg/SLACK_CHANNEL)
        emoji (get props cfg/SLACK_EMOJI)
        username (get props cfg/SLACK_USERNAME_TEMPLATE)
        message (get props cfg/SLACK_MESSAGE_TEMPLATE)]
    (http/post
     webhook
     {:headers {"Content-type" "application/json"}
      :body (json/generate-string
             (cond-> {:text (render-template message topic key value)}
               (not-empty channel) (assoc :channel channel)
               (not-empty emoji) (assoc :icon_emoji emoji)
               (not-empty username) (assoc :username (render-template username topic key value))))})))
