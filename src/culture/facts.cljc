(ns culture.facts
  "Country-level regional-culture catalog for Latvia (LVA) -- national
  dishes, protected products, beverages, crafts, festivals and heritage
  sites, per ADR-2607171400 addendum 2 (cloud-itonami-municipality-
  culture-catalog Wave 1, in com-junkawasaki/root). Sibling namespace to
  `marketentry.facts` / `statute.facts` (ADR-2607141700); city-level
  counterparts live in the cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of culture entries."
  {"LVA"
   [{:culture/id "lva.dish.rupjmaize"
     :culture/name "Rupjmaize"
     :culture/country "LVA"
     :culture/kind :dish
     :culture/summary "Traditional dark bread made from rye, considered the staple of the Latvian diet."
     :culture/url "https://en.wikipedia.org/wiki/Rupjmaize"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lva.dish.piragi"
     :culture/name "Pīrāgi"
     :culture/name-local "Speķa pīrāgi"
     :culture/country "LVA"
     :culture/kind :dish
     :culture/summary "Crescent-shaped buns of leavened dough, called speķrauši or speķa pīrāgi in Latvia, traditionally filled with smoked fatback and onion."
     :culture/url "https://en.wikipedia.org/wiki/Pirozhki"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lva.dish.grey-peas"
     :culture/name "Grey peas with bacon"
     :culture/name-local "Pelēkie zirņi ar speķi"
     :culture/country "LVA"
     :culture/kind :dish
     :culture/summary "Traditional Latvian Christmas dish of large grey peas with bacon; Latvian large grey peas were entered into the EU Register of protected geographical indications for national products in 2015."
     :culture/url "https://en.wikipedia.org/wiki/Grey_peas"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lva.beverage.riga-black-balsam"
     :culture/name "Riga Black Balsam"
     :culture/country "LVA"
     :culture/kind :beverage
     :culture/summary "Traditional Latvian herbal balsam often considered the national drink of Latvia, a medicinal drink created in 1752 that has evolved into a popular liqueur."
     :culture/url "https://en.wikipedia.org/wiki/Riga_Black_Balsam"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lva.craft.lielvarde-belt"
     :culture/name "Lielvārde belt"
     :culture/country "LVA"
     :culture/kind :craft
     :culture/summary "Traditional Latvian woven belt with 22 ancient symbols, whose design appears on Latvian banknotes and has inspired artists and folklore enthusiasts."
     :culture/url "https://en.wikipedia.org/wiki/Lielv%C4%81rde_Belt"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lva.festival.jani"
     :culture/name "Jāņi"
     :culture/country "LVA"
     :culture/kind :festival
     :culture/summary "Annual Latvian summer solstice festival celebrated on 23-24 June, featuring wreath-making, bonfires, singing and the symbolic use of plants for renewal and fertility."
     :culture/url "https://en.wikipedia.org/wiki/J%C4%81%C5%86i"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "lva.heritage.historic-centre-of-riga"
     :culture/name "Historic Centre of Riga"
     :culture/country "LVA"
     :culture/kind :heritage
     :culture/summary "Vecrīga (Old Riga) is part of a UNESCO World Heritage Site listed as \"Historic Centre of Riga\", designated in 1997."
     :culture/url "https://en.wikipedia.org/wiki/Vecr%C4%ABga"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-lva culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "LVA"))
                 " LVA entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
