package com.andrewkingmarshall.pexels.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * This Base Fragment makes View Binding much easier.
 *
 * [Credit](https://chetangupta.net/viewbinding/#base)
 *
 * @param VB The ViewBinding Type
 * @property bindingFactory A lambda that can create a binding object.
 */
abstract class BaseFragment<VB : ViewBinding>(private val bindingFactory: (LayoutInflater, ViewGroup?, Boolean) -> VB) : Fragment() {

    private var _binding: ViewBinding? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VB
        get() = _binding as VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingFactory.invoke(inflater, container, false)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup(view)
    }

    abstract fun setup(view: View)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}