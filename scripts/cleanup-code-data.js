#!/usr/bin/env node
/**
 * scripts/cleanup-code-data.js
 * One-time administrative script to inspect and clean up leftover verification code
 * and device trust documents from Firestore.
 * 
 * Safety features:
 * - Never runs automatically; never exposed as an API endpoint.
 * - Prints summary counts ONLY (no emails or user identifiers).
 * - Requires explicit user confirmation ("YES") before performing any deletions.
 */

const readline = require('readline');
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    const sa = typeof process.env.FIREBASE_SERVICE_ACCOUNT === 'string'
      ? JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)
      : process.env.FIREBASE_SERVICE_ACCOUNT;
    admin.initializeApp({ credential: admin.credential.cert(sa) });
  } else {
    admin.initializeApp();
  }
}

const db = admin.firestore();

async function main() {
  console.log('======================================================');
  console.log('   Linq Studio - Cleanup Obsolete Verification Data   ');
  console.log('======================================================\n');
  console.log('Scanning Firestore for leftover documents...');

  let authCodesCount = 0;
  let authCodesDocs = [];
  try {
    const authCodesSnap = await db.collection('authCodes').get();
    authCodesCount = authCodesSnap.size;
    authCodesDocs = authCodesSnap.docs;
  } catch (err) {
    console.log(`[Notice] Could not scan authCodes collection: ${err.message}`);
  }

  let trustedDevicesCount = 0;
  let trustedDevicesDocs = [];
  try {
    const devicesSnap = await db.collectionGroup('trustedDevices').get();
    trustedDevicesCount = devicesSnap.size;
    trustedDevicesDocs = devicesSnap.docs;
  } catch (err) {
    console.log(`[Notice] Could not scan trustedDevices collectionGroup: ${err.message}`);
  }

  console.log('\n--- Leftover Data Inventory ---');
  console.log(`- authCodes documents:       ${authCodesCount}`);
  console.log(`- trustedDevices documents:  ${trustedDevicesCount}`);
  console.log('-------------------------------\n');

  if (authCodesCount === 0 && trustedDevicesCount === 0) {
    console.log('No leftover verification documents found. Your Firestore is clean!');
    process.exit(0);
  }

  // Interactive confirmation
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });

  rl.question('Would you like to delete these leftover documents? Type YES to proceed: ', async (answer) => {
    rl.close();

    if (answer.trim() !== 'YES') {
      console.log('Aborted. No documents were deleted.');
      process.exit(0);
    }

    console.log('\nDeleting leftover documents in batches...');

    let deletedCodes = 0;
    if (authCodesDocs.length > 0) {
      const batch = db.batch();
      for (const doc of authCodesDocs) {
        batch.delete(doc.ref);
        deletedCodes++;
      }
      await batch.commit();
    }

    let deletedDevices = 0;
    if (trustedDevicesDocs.length > 0) {
      const batch = db.batch();
      for (const doc of trustedDevicesDocs) {
        batch.delete(doc.ref);
        deletedDevices++;
      }
      await batch.commit();
    }

    console.log(`✓ Cleanup complete: Deleted ${deletedCodes} authCodes and ${deletedDevices} trustedDevices.`);
    process.exit(0);
  });
}

if (require.main === module) {
  main().catch(err => {
    console.error('Fatal error during cleanup inspection:', err);
    process.exit(1);
  });
}

module.exports = { main };
