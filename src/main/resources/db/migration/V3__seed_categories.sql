-- ============================================================
-- Shoppiq
-- V3__seed_categories.sql
--
-- Seeds initial product categories (Electronics, Clothing, etc.).
-- ============================================================

INSERT INTO categories (version,
                        created_at,
                        updated_at,
                        name,
                        slug,
                        description)
VALUES
-- Electronics
(0, NOW(6), NOW(6), 'Electronics', 'electronics',
 'Electronic devices and accessories'),

-- Fashion
(0, NOW(6), NOW(6), 'Fashion', 'fashion',
 'Clothing, footwear and fashion accessories'),

-- Home & Kitchen
(0, NOW(6), NOW(6), 'Home & Kitchen', 'home-kitchen',
 'Home appliances, furniture and kitchen essentials'),

-- Books
(0, NOW(6), NOW(6), 'Books', 'books',
 'Books, magazines and educational material'),

-- Grocery
(0, NOW(6), NOW(6), 'Grocery', 'grocery',
 'Daily grocery and household essentials'),

-- Beauty
(0, NOW(6), NOW(6), 'Beauty & Personal Care', 'beauty-personal-care',
 'Cosmetics, skincare and personal care products'),

-- Health
(0, NOW(6), NOW(6), 'Health', 'health',
 'Healthcare products, supplements and wellness'),

-- Sports
(0, NOW(6), NOW(6), 'Sports & Outdoors', 'sports-outdoors',
 'Sports equipment and outdoor gear'),

-- Toys
(0, NOW(6), NOW(6), 'Toys & Games', 'toys-games',
 'Toys, puzzles and games for all ages'),

-- Baby
(0, NOW(6), NOW(6), 'Baby Products', 'baby-products',
 'Baby care, feeding and parenting essentials'),

-- Automotive
(0, NOW(6), NOW(6), 'Automotive', 'automotive',
 'Vehicle accessories and automotive products'),

-- Office
(0, NOW(6), NOW(6), 'Office Supplies', 'office-supplies',
 'Office stationery and workplace essentials'),

-- Pet Supplies
(0, NOW(6), NOW(6), 'Pet Supplies', 'pet-supplies',
 'Food, accessories and healthcare products for pets'),

-- Garden
(0, NOW(6), NOW(6), 'Garden & Outdoor', 'garden-outdoor',
 'Gardening tools and outdoor living products'),

-- Jewellery
(0, NOW(6), NOW(6), 'Jewellery', 'jewellery',
 'Jewellery, watches and luxury accessories'),

-- Footwear
(0, NOW(6), NOW(6), 'Footwear', 'footwear',
 'Shoes, sandals and boots'),

-- Bags
(0, NOW(6), NOW(6), 'Bags & Luggage', 'bags-luggage',
 'Travel luggage, backpacks and handbags'),

-- Furniture
(0, NOW(6), NOW(6), 'Furniture', 'furniture',
 'Furniture for home and office'),

-- Musical Instruments
(0, NOW(6), NOW(6), 'Musical Instruments', 'musical-instruments',
 'Musical instruments and accessories'),

-- Software
(0, NOW(6), NOW(6), 'Software', 'software',
 'Software licenses and digital products');