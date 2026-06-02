// AuthBridge.swift
//
// Bridges native iOS sign-in (Google + Apple) into the shared Kotlin AuthRepository.
// Call from SwiftUI buttons OR expose to Kotlin via a Koin-registered protocol.
//
// Prerequisites in Xcode:
//   1. SPM: add https://github.com/google/GoogleSignIn-iOS  (GoogleSignIn)
//   2. SPM: firebase-ios-sdk → FirebaseCore, FirebaseAuth (already needed)
//   3. Info.plist → add URL scheme = REVERSED_CLIENT_ID from GoogleService-Info.plist
//   4. Signing & Capabilities → + Sign in with Apple

import AuthenticationServices
import CryptoKit
import Foundation
import GoogleSignIn
import Shared
import UIKit

@MainActor
final class AuthBridge: NSObject {

    static let shared = AuthBridge()

    // MARK: Google

    func signInWithGoogle(presenting: UIViewController) {
        GIDSignIn.sharedInstance.signIn(withPresenting: presenting) { result, error in
            guard
                let idToken = result?.user.idToken?.tokenString,
                error == nil
            else { return }
            // Call shared AuthRepository
            let repo = KoinKt.getAuthRepository()
            Task {
                _ = try? await repo.signInWithGoogleCredential(
                    idToken: idToken,
                    defaultUserType: UserType.client
                )
            }
        }
    }

    // MARK: Apple

    private var currentNonce: String?

    func signInWithApple() {
        let nonce = randomNonceString()
        currentNonce = nonce
        let provider = ASAuthorizationAppleIDProvider()
        let request = provider.createRequest()
        request.requestedScopes = [.fullName, .email]
        request.nonce = sha256(nonce)

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    private func randomNonceString(length: Int = 32) -> String {
        let chars: [Character] = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._")
        var random = [UInt8](repeating: 0, count: length)
        _ = SecRandomCopyBytes(kSecRandomDefault, length, &random)
        return String(random.map { chars[Int($0) % chars.count] })
    }

    private func sha256(_ input: String) -> String {
        let data = Data(input.utf8)
        let hash = SHA256.hash(data: data)
        return hash.map { String(format: "%02x", $0) }.joined()
    }
}

extension AuthBridge: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController,
                                 didCompleteWithAuthorization authorization: ASAuthorization) {
        guard
            let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = credential.identityToken,
            let idToken = String(data: tokenData, encoding: .utf8),
            let nonce = currentNonce
        else { return }
        let repo = KoinKt.getAuthRepository()
        Task {
            _ = try? await repo.signInWithAppleCredential(
                idToken: idToken,
                rawNonce: nonce,
                defaultUserType: UserType.client
            )
        }
    }

    func authorizationController(controller: ASAuthorizationController,
                                 didCompleteWithError error: Error) {
        // Surface to user / log
    }
}

extension AuthBridge: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first ?? ASPresentationAnchor()
    }
}
