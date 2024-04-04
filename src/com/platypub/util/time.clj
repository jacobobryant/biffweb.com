(ns com.platypub.util.time)

(defn parse-date-local [date-str]
  (.parse (doto (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss a")
             (.setTimeZone (java.util.TimeZone/getTimeZone "America/Los_Angeles"))) date-str))
