(ns nucleus.core
  (:refer-clojure :rename {swap! core-swap!
                           reset! core-reset!}))

(def nucleus
  (atom {:views {}}))

(defn init
  [d]
  (core-swap! nucleus #(assoc % :data d)))

(defn view
  [k f]
  (core-swap! nucleus #(assoc-in % [:views k] {:subs []}))
  (add-watch nucleus k
             (fn [k r old new]
               (if (not= (:data old) (:data new))
                 (let [old-view (get-in old [:views k :prev])
                       new-view (f k (:data old) (:data new))]
                   (if (not= old-view new-view)
                     (doseq [f (get-in old [:views k :subs])]
                       (f new-view))))))))

(defn subscribe
  [k f]
  (core-swap! nucleus #(assoc-in % [:views k :subs]
                                 (conj (get-in % [:views k :subs]) f))))

(defn swap!
  [f]
  (core-swap! nucleus #(assoc % :data (f (:data %)))))
