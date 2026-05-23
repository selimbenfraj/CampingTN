#!/usr/bin/env python3
"""
CampingTN ML API — Flask REST server
Serves budget prediction, equipment recommendation, and weather risk models.
Run: python app.py
Port: 5000
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import pandas as pd
import numpy as np
import os
import logging
from datetime import datetime

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# ─── Model Loading ───────────────────────────────────────────────────────────
MODELS_DIR = os.path.dirname(os.path.abspath(__file__))

def load_model(filename):
    path = os.path.join(MODELS_DIR, filename)
    if os.path.exists(path):
        try:
            model = joblib.load(path)
            logger.info(f"✅ Loaded {filename}")
            return model
        except Exception as e:
            logger.error(f"❌ Failed to load {filename}: {e}")
    else:
        logger.warning(f"⚠️  Model not found: {filename} — using rule-based fallback")
    return None

budget_model    = load_model("camping_budget_model.pkl")
equipment_model = load_model("equipment_classifier.pkl")
weather_model   = load_model("weather_risk_model.pkl")
recommender     = load_model("recommender.pkl")

# ─── Static Reference Data ────────────────────────────────────────────────────
GOVERNORATE_INDEX = {
    'Tunis':1.30,'Ariana':1.20,'Ben Arous':1.20,'Manouba':1.00,
    'Bizerte':1.10,'Nabeul':1.15,'Zaghouan':0.95,'Beja':0.90,
    'Jendouba':0.88,'Le Kef':0.87,'Siliana':0.85,
    'Sousse':1.20,'Monastir':1.20,'Mahdia':1.10,
    'Sfax':1.05,'Kairouan':0.90,'Kasserine':0.85,'Sidi Bouzid':0.83,
    'Gabès':0.95,'Médenine':1.00,'Tataouine':0.95,
    'Gafsa':0.88,'Tozeur':1.00,'Kébili':0.92,
}

SEASON_MULT = {
    ('COASTAL','SUMMER'):1.50,('COASTAL','SPRING'):1.10,('COASTAL','AUTUMN'):1.10,('COASTAL','WINTER'):0.85,
    ('DESERT','WINTER'):1.30,('DESERT','SPRING'):1.30,('DESERT','AUTUMN'):1.30,('DESERT','SUMMER'):0.70,
    ('FOREST','SPRING'):1.20,('FOREST','AUTUMN'):1.20,('FOREST','SUMMER'):1.10,('FOREST','WINTER'):0.90,
}

DISTANCE_KM = {
    'Tunis':0,'Ariana':15,'Ben Arous':25,'Manouba':20,
    'Bizerte':65,'Nabeul':80,'Zaghouan':60,'Beja':110,
    'Jendouba':160,'Le Kef':175,'Siliana':140,
    'Sousse':140,'Monastir':160,'Mahdia':200,
    'Sfax':270,'Kairouan':155,'Kasserine':250,'Sidi Bouzid':250,
    'Gabès':360,'Médenine':430,'Tataouine':500,
    'Gafsa':360,'Tozeur':450,'Kébili':430,
}

TYPICAL_WEATHER = {
    'COASTAL':{'SUMMER':(32,70),'WINTER':(13,80),'SPRING':(22,70),'AUTUMN':(20,72)},
    'FOREST': {'SUMMER':(30,65),'WINTER':(10,85),'SPRING':(20,72),'AUTUMN':(18,75)},
    'DESERT': {'SUMMER':(44,18),'WINTER':(15,42),'SPRING':(28,28),'AUTUMN':(25,25)},
}

EQUIPMENT_RULES = {
    'DESERT': {
        'SUMMER': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Desert Tent',
                   'Extra Water (5L/person)','UV Protection Clothing','GPS/Compass',
                   'Cooking Stove','Sunscreen SPF50+','Sand Goggles','Insulated Cooler'],
        'WINTER': ['First Aid Kit','Headlamp','Warm Sleeping Bag (-5°C)','Desert Tent',
                   'Extra Water (3L/person)','GPS/Compass','Cooking Stove','UV Clothing','Sunscreen'],
        'SPRING': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Desert Tent',
                   'Extra Water (4L/person)','UV Clothing','GPS/Compass','Cooking Stove',
                   'Sunscreen SPF50+','Sand Goggles'],
        'AUTUMN': ['First Aid Kit','Headlamp','Warm Sleeping Bag','Desert Tent',
                   'Extra Water (3L/person)','GPS/Compass','Cooking Stove','Sunscreen'],
    },
    'COASTAL': {
        'SUMMER': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Coastal Tent',
                   'Water Bottles','Sunscreen SPF50+','Life Jacket','Snorkel Set',
                   'Cooking Stove','Insect Repellent','After-Sun Lotion'],
        'WINTER': ['First Aid Kit','Headlamp','Warm Sleeping Bag','Coastal Tent',
                   'Water Bottles','Rain Poncho','Cooking Stove','Waterproof Bag'],
        'SPRING': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Coastal Tent',
                   'Water Bottles','Sunscreen','Life Jacket','Cooking Stove','Insect Repellent'],
        'AUTUMN': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Coastal Tent',
                   'Water Bottles','Sunscreen','Cooking Stove','Life Jacket'],
    },
    'FOREST': {
        'SUMMER': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Forest Tent',
                   'Water Purification Filter','Water Bottles','Hiking Boots',
                   'Insect Repellent','Cooking Stove','Sunscreen'],
        'WINTER': ['First Aid Kit','Headlamp','Warm Sleeping Bag (-5°C)','Forest Tent',
                   'Water Purification Filter','Water Bottles','Hiking Boots',
                   'Rain Poncho','Cooking Stove','Thermal Underwear'],
        'SPRING': ['First Aid Kit','Headlamp','Summer Sleeping Bag','Forest Tent',
                   'Water Purification Filter','Water Bottles','Hiking Boots',
                   'Insect Repellent','Rain Poncho','Cooking Stove'],
        'AUTUMN': ['First Aid Kit','Headlamp','Warm Sleeping Bag','Forest Tent',
                   'Water Purification Filter','Water Bottles','Hiking Boots',
                   'Insect Repellent','Rain Poncho','Cooking Stove'],
    },
}

TIPS_DB = {
    ('DESERT','SUMMER'): ['⚠️ Extreme heat — avoid midday activity (11am–4pm)',
                           'Carry 5L water/person/day minimum','Desert nights cool to ~15°C — bring a jacket',
                           'Download offline GPS map before departure','Tell someone your route & return time'],
    ('DESERT','WINTER'): ['Best time to visit the Sahara — mild days, cold nights',
                           'Night temperatures can reach 5°C — warm sleeping bag essential',
                           'Book camel trek experiences in advance','Star gazing is spectacular in winter desert'],
    ('COASTAL','SUMMER'): ['Book 4-6 weeks ahead — high season fills fast',
                            'Check jellyfish alerts before swimming','Water sports peak season',
                            'Sunscreen every 2 hours — Mediterranean UV is intense'],
    ('FOREST','SUMMER'): ['🔥 High fire risk — strictly no open fires outside designated areas',
                           'Tick season — check skin after every hike','Carry water purification tablets'],
    ('FOREST','SPRING'): ['Best season for forest camping in Tunisia','Wildflowers bloom March–April',
                           'Birdwatching season — bring binoculars'],
}

def get_season(month: int) -> str:
    if month in [12, 1, 2]: return 'WINTER'
    if month in [3, 4, 5]:  return 'SPRING'
    if month in [6, 7, 8]:  return 'SUMMER'
    return 'AUTUMN'

def rule_based_budget(gov, site_type, persons, days, season, accom_type):
    gov_idx   = GOVERNORATE_INDEX.get(gov, 1.0)
    s_mult    = SEASON_MULT.get((site_type, season), 1.0)
    dist      = DISTANCE_KM.get(gov, 150)
    base_accom = {
        ('TENT','COASTAL'):18,('TENT','FOREST'):15,('TENT','DESERT'):12,
        ('BUILDING','COASTAL'):35,('BUILDING','FOREST'):28,('BUILDING','DESERT'):32,
    }.get((accom_type, site_type), 20)

    accom     = base_accom * persons * days * gov_idx * s_mult
    food      = 35 * gov_idx * persons * days
    transport = dist * 2 * 0.35 + (50 if persons > 4 else 0)
    equip     = {'DESERT':45,'FOREST':30,'COASTAL':25}.get(site_type,30) * persons + (days*5 if days>3 else 0)
    misc      = ({'DESERT':80,'COASTAL':60,'FOREST':30}.get(site_type,40) + days*10) * min(persons,4)
    total     = accom + food + transport + equip + misc
    margin    = total * 0.15

    return {
        'predicted_budget': round(total, 2),
        'budget_min':        round(total - margin, 2),
        'budget_max':        round(total + margin, 2),
        'breakdown': {
            'Accommodation':    round(accom, 2),
            'Food & Water':     round(food, 2),
            'Transport':        round(transport, 2),
            'Equipment':        round(equip, 2),
            'Activities & Misc':round(misc, 2),
        }
    }

# ─── Routes ──────────────────────────────────────────────────────────────────

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        'status': 'ok',
        'timestamp': datetime.now().isoformat(),
        'models': {
            'budget':     budget_model    is not None,
            'equipment':  equipment_model is not None,
            'weather':    weather_model   is not None,
            'recommender':recommender     is not None,
        }
    })

@app.route('/predict/budget', methods=['POST'])
def predict_budget():
    data = request.get_json()
    gov       = data.get('governorate', 'Tunis')
    site_type = data.get('siteType', 'COASTAL')
    persons   = int(data.get('numberOfPersons', 4))
    days      = int(data.get('numberOfDays', 3))
    accom     = data.get('accommodationType', 'TENT')
    start     = data.get('startDate', '')
    month     = int(start.split('-')[1]) if start and len(start) >= 7 else datetime.now().month
    season    = get_season(month)

    try:
        if budget_model:
            enc  = budget_model['encoders']
            meta = budget_model['metadata']
            gov_idx  = meta['governorate_index'].get(gov, 1.0)
            dist     = meta['distance_km'].get(gov, 150)
            s_mult   = meta['season_multipliers'].get((site_type, season), 1.0)
            temp, hum = meta['typical_weather'].get(site_type,{}).get(season,(25,60))
            heat_idx  = temp * (1 - hum/100)
            is_south  = int(gov in ['Kébili','Tataouine','Tozeur','Gafsa','Médenine'])
            is_coast  = int(gov in ['Nabeul','Bizerte','Sousse','Monastir','Mahdia','Sfax'])

            def se(encoder, val, default=0):
                try: return int(encoder.transform([val])[0])
                except: return default

            x = pd.DataFrame([{
                'governorate_enc':       se(enc['governorate'],       gov),
                'site_type_enc':         se(enc['site_type'],         site_type),
                'season_enc':            se(enc['season'],            season),
                'accommodation_type_enc':se(enc['accommodation_type'],accom),
                'num_persons':           persons,
                'num_days':              days,
                'persons_days':          persons * days,
                'gov_cost_index':        gov_idx,
                'season_multiplier':     s_mult,
                'distance_km':           dist,
                'temperature':           temp,
                'humidity':              hum,
                'heat_index':            heat_idx,
                'is_southern':           is_south,
                'is_coastal_region':     is_coast,
            }])
            pred    = float(budget_model['model'].predict(x)[0])
            rf_pred = float(budget_model['rf'].predict(x)[0])
            gb_pred = float(budget_model['gb'].predict(x)[0])
            margin  = abs(rf_pred - gb_pred) / 2

            rb        = rule_based_budget(gov, site_type, persons, days, season, accom)
            breakdown = rb['breakdown']
            budget_val= round(pred, 2)
        else:
            rb         = rule_based_budget(gov, site_type, persons, days, season, accom)
            budget_val = rb['predicted_budget']
            margin     = rb['predicted_budget'] * 0.15
            breakdown  = rb['breakdown']
            temp, hum  = TYPICAL_WEATHER.get(site_type,{}).get(season,(25,60))

        essential = EQUIPMENT_RULES.get(site_type, {}).get(season, [])
        optional  = ['Camera/GoPro','Portable Speaker','Power Bank','Hammock','Star Map App']
        tips      = TIPS_DB.get((site_type, season), [
            f'Book early for {season.lower()} season',
            'Always carry a first aid kit',
            f'Download offline map of {gov} before departure',
        ])
        alert = None
        if site_type == 'DESERT' and season == 'SUMMER':
            alert = '🔴 EXTREME HEAT: Temperatures exceed 45°C. Not recommended for children or elderly.'
        elif site_type == 'FOREST' and season == 'SUMMER':
            alert = '🟠 FIRE RISK: High fire danger in forests during summer. Follow all safety rules.'
        elif site_type == 'COASTAL' and season == 'WINTER':
            alert = '🟡 ROUGH SEAS: Avoid open-water activities in winter.'

        return jsonify({
            'governorate':      gov,
            'siteType':         site_type,
            'numberOfPersons':  persons,
            'numberOfDays':     days,
            'season':           season,
            'predictedBudget':  budget_val,
            'budgetMin':        round(budget_val - margin, 2),
            'budgetMax':        round(budget_val + margin, 2),
            'budgetBreakdown':  breakdown,
            'essentialItems':   essential,
            'optionalItems':    optional,
            'tips':             tips,
            'weatherAlert':     alert,
            'temperature':      temp,
            'humidity':         hum,
            'weatherSummary':   f'Typical {season.lower()} in {gov}: {temp}°C, {hum}% humidity',
        })
    except Exception as e:
        logger.error(f"Budget prediction error: {e}")
        rb = rule_based_budget(gov, site_type, persons, days, season, accom)
        return jsonify({**rb, 'governorate':gov, 'siteType':site_type,
                        'season':season, 'error_fallback': str(e)}), 200

@app.route('/predict/equipment', methods=['POST'])
def predict_equipment():
    data      = request.get_json()
    site_type = data.get('siteType', 'COASTAL')
    season    = data.get('season', 'SPRING')
    gov       = data.get('governorate', 'Tunis')
    persons   = int(data.get('numberOfPersons', 4))
    days      = int(data.get('numberOfDays', 3))

    essential = EQUIPMENT_RULES.get(site_type, {}).get(season, [])
    optional  = ['Camera/GoPro','Portable Speaker','Power Bank','Hammock',
                 'Binoculars','Journal & Pen','Star Map App','Folding Chair']

    return jsonify({
        'siteType':   site_type,
        'season':     season,
        'essential':  essential,
        'optional':   optional,
        'totalItems': len(essential) + len(optional),
    })

@app.route('/predict/weather-risk', methods=['POST'])
def predict_weather_risk():
    data      = request.get_json()
    gov       = data.get('governorate', 'Tunis')
    site_type = data.get('siteType', 'COASTAL')
    season    = data.get('season', 'SPRING')
    temp, hum = TYPICAL_WEATHER.get(site_type, {}).get(season, (25, 60))

    risk_map = {
        ('DESERT','SUMMER'): ('EXTREME', '🔴', 'Do not go without experienced guide'),
        ('DESERT','WINTER'): ('LOW',     '🟢', 'Ideal season for desert camping'),
        ('DESERT','SPRING'): ('LOW',     '🟢', 'Good conditions — carry extra water'),
        ('DESERT','AUTUMN'): ('LOW',     '🟢', 'Good conditions — carry extra water'),
        ('COASTAL','SUMMER'):('MEDIUM',  '🟡', 'Hot but manageable with precautions'),
        ('COASTAL','WINTER'):('LOW',     '🟢', 'Cool and quiet — ideal for nature lovers'),
        ('FOREST','SUMMER'): ('HIGH',    '🟠', 'High fire risk — strict fire safety required'),
        ('FOREST','WINTER'): ('LOW',     '🟢', 'Cold but safe — warm gear essential'),
    }
    risk_level, icon, advice = risk_map.get((site_type, season), ('LOW','🟢','Good conditions'))

    return jsonify({
        'governorate': gov, 'siteType': site_type, 'season': season,
        'riskLevel': risk_level, 'riskIcon': icon, 'advice': advice,
        'temperature': temp, 'humidity': hum,
    })

@app.route('/recommend/centers', methods=['POST'])
def recommend_centers():
    data      = request.get_json()
    site_type = data.get('siteType', 'COASTAL')
    is_south  = data.get('isSouth', False)
    is_north  = data.get('isNorth', False)
    min_rating= float(data.get('minRating', 4.0))

    all_centers = [
        {'id':'1','name':'Centre Douz','governorate':'Kébili','siteType':'DESERT','rating':4.9,'capacity':230},
        {'id':'2','name':'Centre Marsa El Kssiba','governorate':'Médenine','siteType':'COASTAL','rating':4.7,'capacity':264},
        {'id':'3','name':'Centre El Salloume','governorate':'Sousse','siteType':'COASTAL','rating':4.6,'capacity':130},
        {'id':'4','name':'Centre Beni Mtir','governorate':'Jendouba','siteType':'FOREST','rating':4.5,'capacity':100},
        {'id':'5','name':'Centre El Cheaanbi','governorate':'Kasserine','siteType':'DESERT','rating':4.4,'capacity':86},
        {'id':'6','name':'Centre El Douirat','governorate':'Tataouine','siteType':'DESERT','rating':4.8,'capacity':0},
        {'id':'7','name':'Centre El Douirat (Mahdia)','governorate':'Mahdia','siteType':'COASTAL','rating':4.3,'capacity':80},
        {'id':'8','name':'Centre Chat Mami','governorate':'Bizerte','siteType':'COASTAL','rating':4.2,'capacity':75},
    ]

    results = [c for c in all_centers
               if c['siteType'] == site_type and c['rating'] >= min_rating]
    if not results:
        results = [c for c in all_centers if c['rating'] >= min_rating]

    results.sort(key=lambda x: x['rating'], reverse=True)
    return jsonify({'recommendations': results[:5], 'total': len(results)})

@app.route('/analytics/regional', methods=['GET'])
def regional_analytics():
    return jsonify({
        'regions': [
            {'region':'Nord-Ouest','centers':7,'capacity':488,'primary_type':'FOREST'},
            {'region':'Centre-Est','centers':5,'capacity':520,'primary_type':'COASTAL'},
            {'region':'Nord-Est','centers':3,'capacity':170,'primary_type':'COASTAL'},
            {'region':'Sud-Est','centers':3,'capacity':344,'primary_type':'COASTAL'},
            {'region':'Centre-Ouest','centers':2,'capacity':216,'primary_type':'FOREST'},
            {'region':'District-Tunis','centers':2,'capacity':212,'primary_type':'FOREST'},
            {'region':'Sud-Ouest','centers':1,'capacity':230,'primary_type':'DESERT'},
        ],
        'total_centers': 23,
        'total_capacity': 2180,
        'data_year': 2017,
        'source': 'Official Tunisia Camping Survey',
    })

@app.route('/analytics/evolution', methods=['GET'])
def evolution_analytics():
    return jsonify({
        'years': [2002,2004,2006,2008,2010,2012,2014,2016,2017],
        'total': [23,23,23,24,26,26,26,22,23],
        'by_region': {
            'District-Tunis': [2,2,2,2,2,2,2,1,0],
            'Nord-Est':        [4,4,4,4,4,3,3,3,0],
            'Nord-Ouest':      [7,7,7,7,7,7,7,7,0],
            'Centre-Est':      [4,4,4,4,6,7,7,5,1],
            'Centre-Ouest':    [1,1,1,2,2,2,2,2,0],
            'Sud-Est':         [4,4,4,4,4,4,4,3,0],
            'Sud-Ouest':       [1,1,1,1,1,1,1,1,1],
        }
    })

if __name__ == '__main__':
    print("🏕️  CampingTN ML API starting on http://localhost:5000")
    print("   Endpoints: /health /predict/budget /predict/equipment /predict/weather-risk")
    app.run(debug=True, host='0.0.0.0', port=5000)
