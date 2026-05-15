// MongoDB initialization script for CampingTN
db = db.getSiblingDB('camping_tunisia');

// Create collections with validation
db.createCollection('users');
db.createCollection('camping_centers');
db.createCollection('products');
db.createCollection('orders');
db.createCollection('bookings');
db.createCollection('reviews');
db.createCollection('budget_predictions');
db.createCollection('maintenance_tasks');

// Indexes
db.users.createIndex({ email: 1 }, { unique: true });
db.camping_centers.createIndex({ governorate: 1 });
db.camping_centers.createIndex({ siteNature: 1 });
db.products.createIndex({ category: 1 });
db.products.createIndex({ active: 1 });
db.orders.createIndex({ userId: 1 });
db.orders.createIndex({ status: 1 });
db.bookings.createIndex({ userId: 1 });
db.bookings.createIndex({ campingCenterId: 1 });
db.reviews.createIndex({ campingCenterId: 1 });
db.budget_predictions.createIndex({ userId: 1 });
db.maintenance_tasks.createIndex({ status: 1 });
db.maintenance_tasks.createIndex({ priority: 1 });

print('✅ CampingTN MongoDB initialized successfully');
print('   Collections: users, camping_centers, products, orders, bookings, reviews, budget_predictions, maintenance_tasks');
print('   Indexes created on all major query fields');
